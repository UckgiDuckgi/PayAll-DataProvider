package com.example.PayAll_DataProvider.service;

import com.example.PayAll_DataProvider.dto.AccountResponseDto;
import com.example.PayAll_DataProvider.dto.GetAccountsDto;
import com.example.PayAll_DataProvider.entity.Account;
import java.util.*;

public interface MydataService {
	public GetAccountsDto getAccounts(Long userId, String searchTimestamp, String nextPage, int limit);
	public String getLastSearchTimestamp(Long userId);
	public AccountResponseDto getAccountBasicInfo(String accountNum);
}
