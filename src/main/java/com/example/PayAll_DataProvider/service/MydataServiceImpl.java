package com.example.PayAll_DataProvider.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.PayAll_DataProvider.dto.AccountDto;
import com.example.PayAll_DataProvider.dto.GetAccountsDto;
import com.example.PayAll_DataProvider.entity.Account;
import com.example.PayAll_DataProvider.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MydataServiceImpl implements MydataService{
	private final AccountRepository accountRepository;

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
}
