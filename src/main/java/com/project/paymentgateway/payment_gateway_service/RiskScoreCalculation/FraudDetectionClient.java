package com.project.paymentgateway.payment_gateway_service.RiskScoreCalculation;

import FraudDetectionService.FraudDetectionServiceGrpc;
import FraudDetectionService.riskScore;
import FraudDetectionService.transactionDetails;
import com.project.paymentgateway.payment_gateway_service.dao.Payment;
import com.project.paymentgateway.payment_gateway_service.dao.Transaction;
import com.project.paymentgateway.payment_gateway_service.dao.User;
import com.project.paymentgateway.payment_gateway_service.dto.PaymentRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionClient extends FraudDetectionServiceGrpc.FraudDetectionServiceImplBase {

    private static final String SERVER_ADDRESS = "localhost:50051";
    private final ManagedChannel channel;
    private final FraudDetectionServiceGrpc.FraudDetectionServiceBlockingStub stub;

    private FraudDetectionClient() {
        this.channel = ManagedChannelBuilder.forTarget(SERVER_ADDRESS)
                .usePlaintext()
                .build();
        this.stub = FraudDetectionServiceGrpc.newBlockingStub(channel);
        System.out.println("gRPC Channel initialized on server address: "+ SERVER_ADDRESS);
    }

    public FraudDetectionServiceGrpc.FraudDetectionServiceBlockingStub getStub() {
        return stub;
    }

    public double getRiskScore(Payment payment, User user, Transaction transaction) {
        transactionDetails.Builder request = transactionDetails.newBuilder();
        if(payment != null) {
            request.setFailedTransactionsCount(payment.getFailedTransactionsCount());
        }
        if(transaction != null) {
            request.setTransactionId(transaction.getTransactionId());
            request.setTransactionAmount(transaction.getAmount().doubleValue());
        }
        request.setEmailId(user.getEmailId()).setPhoneNumber(user.getContactNumber())
                .setLastUserLogin(user.getLastSuccessfulLogin().getSecond())
                .setFailedLoginAttempts(user.getFailedLoginCount());

        try {
            riskScore response = getStub().calculateRiskScore(request.build());
            return response.getRiskScore();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("Shutting down gRPC channel...");
        channel.shutdown();
    }
}
