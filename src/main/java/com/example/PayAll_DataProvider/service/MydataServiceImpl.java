package com.example.PayAll_DataProvider.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MydataServiceImpl implements MydataService{
	@Override
	public Object getAccountList(String authorization, String transactionId, String apiType, String orgCode,
		String searchTimestamp, String nextPage, int limit) {

		return null;
	}
}
