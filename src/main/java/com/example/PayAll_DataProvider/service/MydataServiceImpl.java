package com.example.PayAll_DataProvider.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.PayAll_DataProvider.entity.Account;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MydataServiceImpl implements MydataService{
	@Override
	public List<Account> getAccounts(Long userId) {
		return null;
	}
}
