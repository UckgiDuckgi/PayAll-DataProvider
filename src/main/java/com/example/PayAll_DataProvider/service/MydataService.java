package com.example.PayAll_DataProvider.service;

import java.util.List;

import com.example.PayAll_DataProvider.dto.AccountResponseDto;
import com.example.PayAll_DataProvider.dto.GetAccountsDto;
import com.example.PayAll_DataProvider.dto.TransactionCreateDto;
import com.example.PayAll_DataProvider.dto.TransactionRequestDto;
import com.example.PayAll_DataProvider.dto.TransactionResponseDto;

public interface MydataService {
	GetAccountsDto getAccounts(Long userId, String searchTimestamp, String nextPage, int limit);

	String getLastSearchTimestamp(Long userId);

	AccountResponseDto getAccountBasicInfo(String accountNum);

	TransactionResponseDto getMydataTransactions(TransactionRequestDto request);

	String setTransaction(Long userId, List<TransactionCreateDto> requestList);
}
