package com.project.paymentgateway.payment_gateway_service.service;

import com.project.paymentgateway.payment_gateway_service.ExceptionHandler.ObjectNotFoundException;
import com.project.paymentgateway.payment_gateway_service.ExceptionHandler.PaymentProcessingException;
import com.project.paymentgateway.payment_gateway_service.RiskScoreCalculation.FraudDetectionClient;
import com.project.paymentgateway.payment_gateway_service.dao.Payment;
import com.project.paymentgateway.payment_gateway_service.dao.Transaction;
import com.project.paymentgateway.payment_gateway_service.dao.User;
import com.project.paymentgateway.payment_gateway_service.dao.Wallet;
import com.project.paymentgateway.payment_gateway_service.dto.ConfirmPaymentResponse;
import com.project.paymentgateway.payment_gateway_service.dto.InitiatePaymentResponse;
import com.project.paymentgateway.payment_gateway_service.dto.PaymentRequest;
import com.project.paymentgateway.payment_gateway_service.dto.TransactionDto;
import com.project.paymentgateway.payment_gateway_service.enums.PaymentStatus;
import com.project.paymentgateway.payment_gateway_service.enums.TransactionStatus;
import com.project.paymentgateway.payment_gateway_service.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private final UserRepo userRepo;
    private final KycRepo kycRepo;
    private final TokenService tokenService;
    private final UserService userService;
    private final PaymentRepo paymentRepo;
    private final TransactionRepo transactionRepo;
    private final WalletRepo walletRepo;
    private final FraudDetectionClient fraudDetectionClient;

    public PaymentService(UserRepo userRepo, KycRepo kycRepo, TokenService tokenService, UserService userService, PaymentRepo paymentRepo, TransactionRepo transactionRepo, WalletRepo walletRepo, FraudDetectionClient fraudDetectionClient) {
        this.userRepo = userRepo;
        this.kycRepo = kycRepo;
        this.tokenService = tokenService;
        this.userService = userService;
        this.paymentRepo = paymentRepo;
        this.transactionRepo = transactionRepo;
        this.walletRepo = walletRepo;
        this.fraudDetectionClient = fraudDetectionClient;
    }

    public InitiatePaymentResponse initiatePayment(HttpServletRequest httpRequest, PaymentRequest paymentRequest) throws ObjectNotFoundException, AuthorizationServiceException, PaymentProcessingException {
        User sender = userRepo.findByUserId(paymentRequest.getUserId());
        User receiver = userRepo.findByUserName(paymentRequest.getPaymentReceiver());
        if(sender == null){
            throw new ObjectNotFoundException("The user with Id: "+paymentRequest.getUserId()+ " is not found");
        }
        if(receiver == null){
            throw new ObjectNotFoundException("The user: "+paymentRequest.getPaymentReceiver()+" is not found");
        }
        if(!validUserAccess(httpRequest, sender)){
            throw new AuthorizationServiceException("The user is not Authorized to perform this action");
        }
        if(!(walletRepo.findByUser(sender).getBalance().compareTo(paymentRequest.getAmount()) >=0)) {
            throw new PaymentProcessingException("Insufficient Account balance to make this payment");
        }

        Transaction transaction = buildTransactionObject(paymentRequest);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transactionRepo.save(transaction);

        Payment payment = buildPaymentObject(paymentRequest, transaction);
        paymentRepo.save(payment);

        return buildPaymentResponse(payment, transaction);
    }

    public ConfirmPaymentResponse confirmPayment(HttpServletRequest httpRequest, Long transactionId, Integer otp) throws PaymentProcessingException{
        Transaction transaction = transactionRepo.findById(transactionId).get();
        Payment payment = paymentRepo.findByTransaction(transaction);
        int failedTransactionsCount = payment.getFailedTransactionsCount();

        if(!validUserAccess(httpRequest, transaction.getSender())){
            throw new AuthorizationServiceException("The user is not Authorized to perform this action");
        }
        if(!Objects.equals(payment.getOtp(), otp)){
            payment.setFailedTransactionsCount(failedTransactionsCount+1);
            throw new PaymentProcessingException("The otp is invalid");
        }
        if (failedTransactionsCount >= 5){
            transaction.setStatus(TransactionStatus.FAILED);
            payment.setStatus(PaymentStatus.FAILED);
            throw new PaymentProcessingException("The payment failed due to multiple failed transactions");
        }
        if(fraudDetectionClient.getRiskScore(payment, transaction.getReceiver(), transaction) >= 0.5){
            throw new PaymentProcessingException("Cannot make money transfer due to security concerns");
        }

        transferMoney(transaction.getSender(), transaction.getReceiver(), transaction.getAmount());

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepo.save(transaction);
        payment.setStatus(PaymentStatus.APPROVED);
        paymentRepo.save(payment);

        return buildPaymentConfirmationResponse(transaction);
    }

    public void transferMoney(User sender, User receiver, BigDecimal amount) {
        Wallet senderWallet = walletRepo.findByUser(sender);
        Wallet receiverWallet = walletRepo.findByUser(receiver);
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
        walletRepo.save(senderWallet);
        walletRepo.save(receiverWallet);
    }

    public List<TransactionDto> retrieveAllTransactions(Long userId) throws ObjectNotFoundException {
        User user = userRepo.findByUserId(userId);
        if(user == null) {
            throw new ObjectNotFoundException("The user with Id: "+userId+" is not found");
        }
        List<Transaction> transactionsList = transactionRepo.findAllBySender(user);
        System.out.println("Total transactions: "+transactionsList.size());
        List<TransactionDto> transactionDtoList = new ArrayList<>();
        for(Transaction transaction : transactionsList){
            System.out.println();
            if(transaction.getStatus().equals(TransactionStatus.SUCCESS)){
                System.out.println("inside");
                transactionDtoList.add(buildTransactionDto(transaction));
            }
        }
        System.out.println(transactionDtoList.size());
        return transactionDtoList;
    }

    public BigDecimal checkBalance(Long userId) throws ObjectNotFoundException {
        User walletOwner = userRepo.findByUserId(userId);
        if(walletOwner == null){
            throw new ObjectNotFoundException("User with Id: "+userId+" not found");
        }
        Wallet wallet = walletRepo.findByUser(walletOwner);
        if(wallet == null){
            throw new ObjectNotFoundException("The user does not have a account setup");
        }
        return wallet.getBalance();
    }

    public TransactionDto buildTransactionDto(Transaction transaction) {
        System.out.println(transaction.getTransactionId());
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setTransactionId(transaction.getTransactionId());
        transactionDto.setTransactionTime(transaction.getTransactionTime());
        transactionDto.setReceiver(transaction.getReceiver().getUserName());
        transactionDto.setPaymentMethod(transaction.getPaymentMethod());
        transactionDto.setAmount(transaction.getAmount());
        return transactionDto;
    }

    public boolean validUserAccess(HttpServletRequest httpRequest, User user){
        String token = tokenService.getTokenFromAuth(httpRequest);
        String userName = tokenService.getUsernameFromToken(token);
        return Objects.equals(userName, user.getUserName());
    }

    public InitiatePaymentResponse buildPaymentResponse(Payment payment, Transaction transaction){
        InitiatePaymentResponse paymentResponse = new InitiatePaymentResponse();
        paymentResponse.setTransactionId(transaction.getTransactionId());
        paymentResponse.setOtp(payment.getOtp().toString());
        paymentResponse.setPaymentStatus(payment.getStatus());
        return paymentResponse;
    }

    public ConfirmPaymentResponse buildPaymentConfirmationResponse(Transaction transaction) {
        ConfirmPaymentResponse paymentResponse = new ConfirmPaymentResponse();
        paymentResponse.setPaymentStatus(PaymentStatus.APPROVED);
        paymentResponse.setTransactionId(transaction.getTransactionId());
        paymentResponse.setProcessedAt(LocalDateTime.now());
        paymentResponse.setAmountTransferred(transaction.getAmount());
        BigDecimal availableBalance = walletRepo.findByUser(transaction.getSender()).getBalance();
        paymentResponse.setAvailableBalance(availableBalance);
        return paymentResponse;
    }

    public Payment buildPaymentObject(PaymentRequest paymentRequest, Transaction transaction){
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.INITIATED);
        int otp = ThreadLocalRandom.current().nextInt(100000, 1000000);
        payment.setOtp(otp);
        payment.setTransaction(transaction);
        payment.setProcessedAt(LocalDateTime.now());
        if(transaction.getStatus().equals(TransactionStatus.FAILED)){
            payment.setFailedTransactionsCount(payment.getFailedTransactionsCount()+1);
        }else{
            payment.setFailedTransactionsCount(payment.getFailedTransactionsCount());
        }
        return payment;
    }

    public Transaction buildTransactionObject(PaymentRequest paymentRequest){
        Transaction transaction = new Transaction();
        transaction.setPaymentMethod(paymentRequest.getPaymentMethod());
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setAmount(paymentRequest.getAmount());
        transaction.setStatus(TransactionStatus.PENDING);
        return transaction;
    }
}
