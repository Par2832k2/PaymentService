package com.project.paymentgateway.payment_gateway_service.service;

import com.project.paymentgateway.payment_gateway_service.dao.KYCInfo;
import com.project.paymentgateway.payment_gateway_service.dao.User;
import com.project.paymentgateway.payment_gateway_service.dao.Wallet;
import com.project.paymentgateway.payment_gateway_service.dto.KycDto;
import com.project.paymentgateway.payment_gateway_service.dto.UserDto;
import com.project.paymentgateway.payment_gateway_service.enums.kycStatus;
import com.project.paymentgateway.payment_gateway_service.repository.KycRepo;
import com.project.paymentgateway.payment_gateway_service.repository.UserRepo;
import com.project.paymentgateway.payment_gateway_service.ExceptionHandler.*;
import com.project.paymentgateway.payment_gateway_service.repository.WalletRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final TokenService tokenService;
    private final ModelMapper modelMapper;
    private final KycRepo kycRepo;
    private final WalletRepo walletRepo;

    public UserService(UserRepo userRepo, @Lazy TokenService tokenService, ModelMapper modelMapper, KycRepo kycRepo, WalletRepo walletRepo){
        this.userRepo = userRepo;
        this.tokenService = tokenService;
        this.modelMapper = modelMapper;
        this.kycRepo = kycRepo;
        this.walletRepo = walletRepo;
    }

    public boolean validateUser(String userName){
        User user = userRepo.findByUserName(userName);
        return user != null;
    }

    // Create a new User
    public User createUser(User user){
        modelMapper.typeMap(UserDto.class, User.class)
                .addMappings(mapper -> mapper.skip(User::setUserId));
        //User user = modelMapper.map(userDto, User.class);
        userRepo.save(user);
        return userRepo.findByUserName(user.getUserName());
        //return userRepo.findByUserName(userDto.getUserName());
    }

//    public User retrieveUserByName(String userName) throws ObjectNotFoundException {
//        User user = userRepo.findByUserName(userName);
//        if(user == null) {
//            throw new ObjectNotFoundException("The user with name: "+userName+" is not found");
//        }
//        return user;
//    }

    // Retrieve all users:
    public List<User> retrieveAllUsers() {
        return userRepo.findAll();
    }

    // Update user details:
    public User updateUser(User user, HttpServletRequest request) throws ValidationException, ObjectNotFoundException, AuthorizationServiceException {
        String userNameFromToken = getUserNameFromHeader(request);
        if(user.getUserId() == null){
            throw new ValidationException("The userId is missing");
        }
        User userFromDB = userRepo.findByUserId(user.getUserId());
        setUpdatedFieds(userFromDB, user);
        if(userFromDB != null){
            if(Objects.equals(userFromDB.getUserName(), userNameFromToken)){
                userRepo.save(userFromDB);
            } else throw new AuthorizationServiceException("The user is not authorized to update this user's details");
        } else {
            throw new ObjectNotFoundException("The user with Id:" + user.getUserId() + "does not exist");
        }
        return userRepo.findByUserId(user.getUserId());
    }

    // Remove a user
    public void deleteUser(Long userId) throws ObjectNotFoundException {
        User userFromDB = userRepo.findByUserId(userId);
        if(userFromDB != null){
            if (userFromDB.getKycInfo() != null) {
                userFromDB.removeKYCInfo();  // Break relationship safely
            }
            userRepo.deleteById(userId);
        } else {
            throw new ObjectNotFoundException("The user with Id:" + userId + "does not exist");
        }
    }

    public KYCInfo registerKyc(KycDto kycDto) throws ObjectNotFoundException {
        User user = userRepo.findByUserId(kycDto.getUserId());
        if(user == null){
            throw new ObjectNotFoundException("The user with Id: "+kycDto.getUserId()+ " is not available");
        }
        KYCInfo kycInfo = new KYCInfo();
        kycInfo.setUser(user);
        kycInfo.setDocumentType(kycDto.getDocumentType());
        String documentId = generateKYCDocumentId(kycDto.getDocumentType().toString(), user.getUserId());
        kycInfo.setDocumentId(documentId);
        kycInfo.setKycStatus(kycStatus.APPROVED);
        kycInfo.setCreatedAt(LocalDateTime.now());
        kycInfo.setUpdatedAt(LocalDateTime.now());
        kycRepo.save(kycInfo);
        KYCInfo newKycInfo = kycRepo.findByDocumentId(documentId);
        user.setKycInfo(newKycInfo);
        userRepo.save(user);
        return newKycInfo;
    }

    public Wallet setupWallet(Long userId, BigDecimal balance) throws ValidationException {
        User walletOwner = userRepo.findByUserId(userId);
        KYCInfo kycInfo = kycRepo.findByUser(walletOwner);
        if(kycInfo == null || !kycInfo.getKycStatus().equals(kycStatus.APPROVED)){
            throw new ValidationException("KYC info need to be setup before setting up wallet");
        }
        Wallet newWallet = new Wallet();
        newWallet.setBalance(balance);
        newWallet.setUser(walletOwner);
        walletRepo.save(newWallet);
        return walletRepo.findByUser(walletOwner);
    }

    public String generateKYCDocumentId(String documentType, Long userId){
        return userId + "-" + documentType + "-" + UUID.randomUUID();
    }

    public String getUserNameFromHeader(HttpServletRequest request){
        String token = tokenService.getTokenFromAuth(request);
        return tokenService.getUsernameFromToken(token);
    }

    public String getRoleFromUserName(String userName) throws ObjectNotFoundException {
        User user = userRepo.findByUserName(userName);
        if(user != null){
            return user.getRole().toString();
        } else {
            throw new ObjectNotFoundException("The user is not available");
        }
    }

    public void setUpdatedFieds(User userFromDB, User user){
        if(user.getUserName() != null){
            userFromDB.setUserName(user.getUserName());
        }
        if(user.getPassword() != null){
            userFromDB.setPassword(user.getPassword());
        }
        if(user.getEmailId() != null){
            userFromDB.setEmailId(user.getEmailId());
        }
        if(user.getContactNumber() != null){
            userFromDB.setContactNumber(user.getContactNumber());
        }
        if(user.getFullName() != null){
            userFromDB.setFullName(user.getFullName());
        }
    }
}
