package com.example.PayAll_DataProvider.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.PayAll_DataProvider.dto.AccountDto;
import com.example.PayAll_DataProvider.dto.AccountResponseDto;
import com.example.PayAll_DataProvider.dto.AccountBasicInfoDto;
import com.example.PayAll_DataProvider.dto.AccountTransactionDto;
import com.example.PayAll_DataProvider.dto.GetAccountsDto;
import com.example.PayAll_DataProvider.dto.TransactionRequestDto;
import com.example.PayAll_DataProvider.dto.TransactionResponseDto;
import com.example.PayAll_DataProvider.entity.Account;
import com.example.PayAll_DataProvider.entity.AccountBasicInfo;
import com.example.PayAll_DataProvider.entity.AccountTransaction;
import com.example.PayAll_DataProvider.repository.AccountBasicInfoRepository;
import com.example.PayAll_DataProvider.repository.AccountRepository;
import com.example.PayAll_DataProvider.repository.AccountTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MydataServiceImpl implements MydataService {
	private final AccountRepository accountRepository;
	private final AccountBasicInfoRepository accountBasicInfoRepository;
	private final AccountTransactionRepository accountTransactionRepository;

	// 최근 조회 시간을 저장하는 Map (Key: UserId, Value: searchTimestamp)
	private final Map<Long, String> lastSearchTimestamp = new HashMap<>();

	@Override
	// 계좌 목록 조회 로직
	public GetAccountsDto getAccounts(Long userId, String searchTimestamp, String nextPage, int limit) {
		if (nextPage != null) {
			searchTimestamp = null;
		}

		if (searchTimestamp == null || searchTimestamp.equals("0")) {
			searchTimestamp = "0";
		} else {
			searchTimestamp = lastSearchTimestamp.getOrDefault(userId, "0");
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

		// 응답 데이터 구성
		GetAccountsDto response = GetAccountsDto.builder()
			.rspCode("0000")
			.rspMsg("정상 처리")
			.searchTimestamp(String.valueOf(System.currentTimeMillis()))
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
				.transDtime(transaction.getTransDtime().toLocalDateTime())
				.transNo(transaction.getTransNo())
				.transType(transaction.getTransType())
				.transTypeDetail(transaction.getTransTypeDetail())
				.transNum(transaction.getTransNum())
				.transUnit(transaction.getTransUnit())
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
}
