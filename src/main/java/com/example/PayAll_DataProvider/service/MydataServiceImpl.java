package com.example.PayAll_DataProvider.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openqa.selenium.NotFoundException;
import org.springframework.stereotype.Service;

import com.example.PayAll_DataProvider.dto.AccountBasicInfoDto;
import com.example.PayAll_DataProvider.dto.AccountDto;
import com.example.PayAll_DataProvider.dto.AccountResponseDto;
import com.example.PayAll_DataProvider.dto.AccountTransactionDto;
import com.example.PayAll_DataProvider.dto.GetAccountsDto;
import com.example.PayAll_DataProvider.dto.TransactionCreateDto;
import com.example.PayAll_DataProvider.dto.TransactionRequestDto;
import com.example.PayAll_DataProvider.dto.TransactionResponseDto;
import com.example.PayAll_DataProvider.entity.Account;
import com.example.PayAll_DataProvider.entity.AccountBasicInfo;
import com.example.PayAll_DataProvider.entity.AccountTransaction;
import com.example.PayAll_DataProvider.entity.User;
import com.example.PayAll_DataProvider.repository.AccountBasicInfoRepository;
import com.example.PayAll_DataProvider.repository.AccountRepository;
import com.example.PayAll_DataProvider.repository.AccountTransactionRepository;
import com.example.PayAll_DataProvider.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MydataServiceImpl implements MydataService {
	private final AccountRepository accountRepository;
	private final AccountBasicInfoRepository accountBasicInfoRepository;
	private final AccountTransactionRepository accountTransactionRepository;

	// 최근 조회 시간을 저장하는 Map (Key: UserId, Value: searchTimestamp)
	private final Map<Long, String> lastSearchTimestamp = new HashMap<>();
	private final UserRepository userRepository;

	@Override
	// 계좌 목록 조회 로직
	public GetAccountsDto getAccounts(Long userId, String searchTimestamp, String nextPage, int limit) {
		System.out.println("searchTimestamp = " + searchTimestamp);
		System.out.println("lastSearchTimestamp = " + lastSearchTimestamp.get(userId));

		if (nextPage != null) {
			searchTimestamp = null;
		}

		// if (searchTimestamp == null || searchTimestamp.equals("0")) {
		// 	searchTimestamp = "0";
		// } else {
		// 	searchTimestamp = lastSearchTimestamp.getOrDefault(userId, "0");
		// }

		String currentTimestamp = lastSearchTimestamp.getOrDefault(userId, "0");

		if (searchTimestamp == null) {
			searchTimestamp = currentTimestamp;
		}

		// 계좌 목록 조회
		List<Account> accounts = accountRepository.findByUserId(userId);

		// 계좌 정보를 Dto로 변환
		List<AccountDto> accountDtos = accounts.stream()
			.map(account -> AccountDto.builder()
				.accountNum(account.getAccountNum())
				.isConsent(true) // isConsent를 true로 가정 (실제 로직에 따라 변경 가능)
				.accountName(account.getAccountName())
				.accountType(account.getAccountType())
				.issueDate(account.getIssueDate().toString())
				.isTaxBenefits(account.getIsTaxBenefits())
				.isCma(account.getIsCma())
				.isStockTrans(account.getIsStockTrans())
				.isAccountLink(account.getIsAccountLink())
				.build()
			)
			.toList();

		LocalDateTime now = LocalDateTime.now();
		String newSearchTimestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

		// Map에 최근 조회 시간 저장
		lastSearchTimestamp.put(userId, newSearchTimestamp);

		// 응답 데이터 구성
		GetAccountsDto response = GetAccountsDto.builder()
			.rspCode("0000")
			.rspMsg("정상 처리")
			.searchTimestamp(searchTimestamp)
			.nextPage(null)
			.accountCnt(accountDtos.size())
			.accountList(accountDtos)
			.build();

		return response;
	}

	@Override
	public String getLastSearchTimestamp(Long userId) {
		return lastSearchTimestamp.getOrDefault(userId, "0");
	}

	@Override
	public AccountResponseDto getAccountBasicInfo(String accountNum) {
		// 계좌 기본 정보 조회
		AccountBasicInfo accountBasicInfo = accountBasicInfoRepository.findByAccountNum(accountNum)
			.orElseThrow(() -> new IllegalArgumentException("해당 계좌 정보를 찾을 수 없습니다."));

		// AccountBasicInfo 데이터를 DTO로 변환
		AccountBasicInfoDto basicInfoDto = AccountBasicInfoDto.builder()
			.accountNum(accountBasicInfo.getAccount().getAccountNum())
			.baseDate(accountBasicInfo.getBaseDate().toString())
			.currencyCode(accountBasicInfo.getCurrencyCode())
			.withholdingsAmt(accountBasicInfo.getWithholdingsAmt())
			.creditLoanAmt(
				accountBasicInfo.getCreditLoanAmt() != null ? accountBasicInfo.getCreditLoanAmt() : BigDecimal.ZERO)
			.mortgageAmt(
				accountBasicInfo.getMortgageAmt() != null ? accountBasicInfo.getMortgageAmt() : BigDecimal.ZERO)
			.availBalance(accountBasicInfo.getAvailBalance())
			.build();

		// 응답 데이터 구성
		AccountResponseDto response = AccountResponseDto.builder()
			.rspCode("0000")
			.rspMsg("정상 처리")
			.baseDate(accountBasicInfo.getBaseDate().toString())
			.basicCnt(1)
			.basicList(Collections.singletonList(basicInfoDto))
			.build();

		return response;
	}

	@Override
	public TransactionResponseDto getMydataTransactions(TransactionRequestDto request) {

		List<AccountTransaction> accountTransactions = accountTransactionRepository.findTransactions(
			request.getAccountNum(),
			request.getFromDate(),
			request.getToDate(),
			request.getLimit(),
			request.getNextPage()
		);

		List<AccountTransactionDto> transactionDtos = accountTransactions.stream()
			.map(transaction -> AccountTransactionDto.builder()
				.prodName(transaction.getProdName())
				.prodCode(transaction.getProdCode())
				.transDtime(transaction.getTransDtime())
				.transNo(transaction.getTransNo())
				.transType(transaction.getTransType())
				.transTypeDetail(transaction.getTransTypeDetail())
				.transNum(transaction.getTransNum())
				.transUnit(transaction.getTransUnit())
				.baseAmt(transaction.getBaseAmt())
				.transAmt(transaction.getTransAmt())
				.settleAmt(transaction.getSettleAmt())
				.balanceAmt(transaction.getBalanceAmt())
				.currencyCode(transaction.getCurrencyCode())
				.transMemo(transaction.getTransMemo())
				.exCode(transaction.getExCode())
				.build()
			)
			.toList();

		// 응답 데이터 구성
		TransactionResponseDto response = TransactionResponseDto.builder()
			.rspCode("0000")
			.rspMsg("정상 처리")
			.nextPage(null)
			.transCnt(transactionDtos.size())
			.transList(transactionDtos)
			.build();

		return response;
	}

	@Override
	public String setTransaction(Long userId, List<TransactionCreateDto> requestList) {
		User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

		Account account = accountRepository.findFirstByUserId(userId)
			.orElseThrow(() -> new NotFoundException("Account not found"));

		AccountBasicInfo accountBasicInfo = accountBasicInfoRepository.findByAccountNum(account.getAccountNum())
			.orElseThrow(() -> new NotFoundException("Account Info not found"));

		BigDecimal updatedBalance = accountBasicInfo.getAvailBalance();

		List<AccountTransaction> transactions = new ArrayList<>();

		for (TransactionCreateDto request : requestList) {
			updatedBalance = updatedBalance.subtract(BigDecimal.valueOf(request.getPrice()));

			// 거래 내역 update
			AccountTransaction accountTransaction = AccountTransaction.builder()
				.accountNum(account.getAccountNum())
				.transType("401")
				.transTypeDetail("출금")
				.transDtime(Timestamp.valueOf(LocalDateTime.now()))
				.transNo(getTransactionNo())
				.prodName(request.getStoreName())
				.prodCode("PAYALL")
				.transAmt(BigDecimal.valueOf(request.getPrice()))
				.settleAmt(BigDecimal.valueOf(request.getPrice()))
				.balanceAmt(updatedBalance)
				.currencyCode("KRW")
				.account(account)
				.build();

			transactions.add(accountTransaction);
		}
		accountTransactionRepository.saveAll(transactions);

		// 계좌 정보에 잔액 update
		accountBasicInfo.setAvailBalance(updatedBalance);
		accountBasicInfoRepository.save(accountBasicInfo);

		return account.getAccountNum();
	}

	// transNo 생성
	private String getTransactionNo() {
		String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String sequence = String.format("%04d", new Random().nextInt(10000));
		return "T" + datePrefix + sequence;
	}

}
