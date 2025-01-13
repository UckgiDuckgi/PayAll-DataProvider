package com.example.PayAll_DataProvider.service;

import com.example.PayAll_DataProvider.dto.LowestPriceDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface CrawlToRedisService {
	void saveProductToRedis();

	LowestPriceDto getProduct(String pCode) throws JsonProcessingException;
}
