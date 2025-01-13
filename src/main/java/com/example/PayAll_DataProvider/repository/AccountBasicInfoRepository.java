package com.example.PayAll_DataProvider.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PayAll_DataProvider.entity.AccountBasicInfo;

@Repository
public interface AccountBasicInfoRepository extends JpaRepository<AccountBasicInfo, Long> {
	Optional<AccountBasicInfo> findByAccountNum(String accountNum);
}
