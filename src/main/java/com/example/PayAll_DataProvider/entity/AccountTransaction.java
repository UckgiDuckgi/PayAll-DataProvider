package com.example.PayAll_DataProvider.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 20)
	private String accountNum;

	@Column(nullable = false, length = 3)
	private String transType;

	@Column(nullable = false, length = 60)
	private String transTypeDetail;

	@Column(nullable = false)
	private Timestamp transDtime;

	@Column(length = 64)
	private String transNo;

	@Column(length = 60)
	private String prodName;

	@Column(length = 12)
	private String prodCode;

	@Column(precision = 21, scale = 8)
	private BigDecimal transNum;

	@Column(length = 30)
	private String transUnit;

	@Column(precision = 20, scale = 8)
	private BigDecimal baseAmt;

	@Column(nullable = false, precision = 18, scale = 3)
	private BigDecimal transAmt;

	@Column(precision = 18, scale = 3)
	private BigDecimal settleAmt;

	@Column(nullable = false, precision = 18, scale = 3)
	private BigDecimal balanceAmt;

	@Column(nullable = false, length = 3)
	private String currencyCode;

	@Column(length = 90)
	private String transMemo;

	@Column(length = 3)
	private String exCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;
}
