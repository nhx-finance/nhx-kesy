```mermaid
sequenceDiagram
    participant C as Client
    participant UC as UserController
    participant MS as MintService
    participant US as UserService
    participant WS as WalletService
    participant NS as NotificationService
    participant MR as MintRequestRepository
    participant WR as WalletRepository
    participant UR as UserRepository
    participant SM as SMTP Server
    participant BC as Blockchain Service
    participant EH as ExceptionHandler

    rect rgb(232, 245, 233)
    Note over C,BC: User Mint Request Submission
    C->>+UC: POST /api/user/mints + JWT
    UC->>UC: validateRequest(amount, walletId)
    
    alt Invalid Amount
        UC->>EH: throw InvalidAmountException
        EH-->>C: 400 BAD_REQUEST
    end
    
    UC->>+MS: createMintRequest(userId, request)
    MS->>+US: getUserById(userId)
    US->>+UR: findById(userId)
    
    alt User Not Found
        UR-->>-US: empty
        US->>EH: throw ResourceNotFoundException
        EH-->>C: 404 NOT_FOUND
    end
    
    UR-->>US: User
    US-->>-MS: User
    
    alt KYC Not Verified
        MS->>MS: checkKycStatus(user)
        MS->>EH: throw KycNotVerifiedException
        EH-->>C: 403 FORBIDDEN
    end
    
    MS->>+WS: getWalletById(walletId)
    WS->>+WR: findById(walletId)
    
    alt Wallet Not Found
        WR-->>-WS: empty
        WS->>EH: throw ResourceNotFoundException
        EH-->>C: 404 NOT_FOUND
    end
    
    WR-->>WS: Wallet
    WS-->>-MS: Wallet
    
    alt Wallet Not Owned By User
        MS->>MS: validateOwnership(wallet, userId)
        MS->>EH: throw UnauthorizedAccessException
        EH-->>C: 403 FORBIDDEN
    end
    
    MS->>MS: calculateFees(amount)
    MS->>MS: validateBusinessRules(amount, user)
    
    MS->>+MR: save(mintRequest)
    MR-->>-MS: MintRequest
    
    MS->>+NS: notifyAdminNewMintRequest(user, request)
    NS->>+SM: sendEmail(adminEmail, subject, template)
    SM-->>-NS: emailSent
    NS-->>-MS: notificationSent
    
    MS->>+NS: notifyUserMintRequestReceived(user, request)
    NS->>+SM: sendEmail(userEmail, subject, template)
    SM-->>-NS: emailSent
    NS-->>-MS: notificationSent
    
    MS-->>-UC: MintRequestResponse
    UC-->>-C: 201 CREATED + request details
    end

    rect rgb(227, 242, 253)
    Note over C,BC: Admin Mint Request Review
    C->>+UC: GET /api/admin/mints + Admin JWT
    UC->>+MS: getAllMintRequests(filters, pagination)
    MS->>+MR: findAll(specification, pageable)
    MR-->>-MS: Page<MintRequest>
    MS-->>-UC: Page<MintRequestDto>
    UC-->>-C: 200 OK + paginated requests
    end

    rect rgb(255, 243, 224)
    Note over C,BC: Admin Approve Mint Request
    C->>+UC: PATCH /api/admin/mints/{id}/status + Admin JWT
    UC->>+MS: updateMintStatus(mintId, APPROVED, adminId)
    MS->>+MR: findById(mintId)
    
    alt Mint Request Not Found
        MR-->>-MS: empty
        MS->>EH: throw ResourceNotFoundException
        EH-->>C: 404 NOT_FOUND
    end
    
    MR-->>MS: MintRequest
    MS->>MS: validateStatusTransition(currentStatus, newStatus)
    
    alt Invalid Status Transition
        MS->>EH: throw InvalidStatusTransitionException
        EH-->>C: 400 BAD_REQUEST
    end
    
    MS->>MR: updateStatus(mintId, APPROVED, adminId)
    
    MS->>+BC: initiateMintTransaction(request)
    BC->>BC: buildTransaction(amount, walletAddress)
    BC->>BC: signTransaction(privateKey)
    BC->>BC: broadcastTransaction()
    
    alt Blockchain Error
        BC-->>-MS: BlockchainException
        MS->>MR: updateStatus(mintId, FAILED, error)
        MS->>EH: throw BlockchainException
        EH-->>C: 500 INTERNAL_SERVER_ERROR
    else Transaction Broadcast Success
        BC-->>MS: txHash
        MS->>MR: updateTransactionHash(mintId, txHash)
        MS->>MR: updateStatus(mintId, PROCESSING)
    end
    
    MS->>+NS: notifyUserMintApproved(user, request, txHash)
    NS->>+SM: sendEmail(userEmail, subject, template)
    SM-->>-NS: emailSent
    NS-->>-MS: notificationSent
    
    MS-->>-UC: MintStatusUpdateResponse
    UC-->>-C: 200 OK + updated status
    end

    rect rgb(255, 235, 238)
    Note over C,BC: Admin Reject Mint Request
    C->>+UC: PATCH /api/admin/mints/{id}/status + Admin JWT
    UC->>+MS: updateMintStatus(mintId, REJECTED, adminId, reason)
    MS->>+MR: findById(mintId)
    MR-->>-MS: MintRequest
    
    MS->>MR: updateStatus(mintId, REJECTED, adminId, reason)
    
    MS->>+NS: notifyUserMintRejected(user, request, reason)
    NS->>+SM: sendEmail(userEmail, subject, template)
    SM-->>-NS: emailSent
    NS-->>-MS: notificationSent
    
    MS-->>-UC: MintStatusUpdateResponse
    UC-->>-C: 200 OK + rejection details
    end

    rect rgb(243, 229, 245)
    Note over BC,MS: Blockchain Transaction Confirmation (Async)
    BC->>BC: pollTransactionStatus(txHash)
    BC->>BC: waitForConfirmations(minConfirmations)
    
    alt Transaction Confirmed
        BC->>+MS: handleTransactionConfirmed(txHash, blockNumber)
        MS->>MR: updateStatus(mintId, COMPLETED, blockNumber)
        MS->>+NS: notifyUserMintCompleted(user, request)
        NS->>SM: sendEmail(userEmail, subject, template)
        NS-->>-MS: notificationSent
        MS-->>-BC: acknowledged
    else Transaction Failed
        BC->>+MS: handleTransactionFailed(txHash, error)
        MS->>MR: updateStatus(mintId, FAILED, error)
        MS->>+NS: notifyUserMintFailed(user, request, error)
        NS->>SM: sendEmail(userEmail, subject, template)
        NS-->>-MS: notificationSent
        MS-->>-BC: acknowledged
    end
    end
```