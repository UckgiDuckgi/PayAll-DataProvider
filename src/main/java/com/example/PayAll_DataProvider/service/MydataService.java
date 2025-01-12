package com.example.PayAll_DataProvider.service;

import com.example.PayAll_DataProvider.entity.Account;
import java.util.*;

public interface MydataService {
	List<Account> getAccounts(Long userId);
}
