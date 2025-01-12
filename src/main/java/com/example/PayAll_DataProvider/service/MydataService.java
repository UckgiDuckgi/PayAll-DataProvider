package com.example.PayAll_DataProvider.service;

public interface MydataService {
	Object getAccountList(String authorization, String transactionId, String apiType, String orgCode,
		String searchTimestamp, String nextPage, int limit);
}
