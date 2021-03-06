package com.thiagofa.aprovafacil.creditcard.it;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;

import javax.xml.bind.JAXBException;


import org.junit.Ignore;
import org.junit.Test;

import com.thiagofa.aprovafacil.creditcard.Acquirer;
import com.thiagofa.aprovafacil.creditcard.Authorization;
import com.thiagofa.aprovafacil.creditcard.CreditCard;
import com.thiagofa.aprovafacil.creditcard.CreditCardHolder;
import com.thiagofa.aprovafacil.creditcard.Currency;
import com.thiagofa.aprovafacil.creditcard.Settlement;
import com.thiagofa.aprovafacil.creditcard.trx.AuthorizationTransaction;
import com.thiagofa.aprovafacil.creditcard.trx.RecurringTransaction;
import com.thiagofa.aprovafacil.creditcard.trx.SettlementTransaction;
import com.thiagofa.aprovafacil.service.AprovaFacilService;
import com.thiagofa.aprovafacil.util.Util;

public class ITTransactionTest {

	private static final String ORDER_NUMBER = "123";
	private static final BigDecimal AMOUNT = new BigDecimal(1.99);

	@Test
	//@Ignore
	public void shouldAuthorizeAndSettleRegularTransaction() throws ParseException, IOException, JAXBException {
		AprovaFacilService aprovaFacilService = new AprovaFacilService("boobow", 
				AprovaFacilService.Environment.TEST);
		
		Authorization authorization = authorizeRegular(aprovaFacilService);
		assertTrue("Regular transaction refused.", authorization.isAuthorized());
		
		Settlement settlement = this.settle(aprovaFacilService, authorization);
		assertTrue("Settlement of regular transaction not confirmed.", settlement.isConfirmed());
	}
	
	@Test
	//@Ignore
	public void shouldAuthorizeAndSettleRecurringTransaction() throws ParseException, IOException, JAXBException {
		AprovaFacilService aprovaFacilService = new AprovaFacilService("boobow", 
				AprovaFacilService.Environment.TEST);
		
		Authorization authorization = authorizeRegular(aprovaFacilService);
		assertTrue("Regular transaction to be used in recurring in next step refused.", authorization.isAuthorized());
		
		authorization = authorizeRecurring(aprovaFacilService, authorization.getTransactionNumber());
		assertTrue("Recurring transaction refused.", authorization.isAuthorized());
		
		Settlement settlement = this.settle(aprovaFacilService, authorization);
		assertTrue("Settlement of recurring transaction not confirmed.", settlement.isConfirmed());
	}
	
	private Settlement settle(AprovaFacilService aprovaFacilService, Authorization authorization) 
			throws IOException, JAXBException {
		SettlementTransaction transaction = new SettlementTransaction();
		transaction.setAprovaFacilService(aprovaFacilService);
		
		transaction.setAmount(AMOUNT);
		transaction.setDocumentNumber(ORDER_NUMBER);
		transaction.setTransactionNumber(authorization.getTransactionNumber());
		transaction.setUtf8Output(true);
		return transaction.settle();
	}

	private Authorization authorizeRecurring(AprovaFacilService aprovaFacilService, 
			String lastTransactionNumber) throws ParseException, IOException, JAXBException {
		RecurringTransaction transaction = new RecurringTransaction();
		transaction.setAprovaFacilService(aprovaFacilService);

		transaction.setLastTransactionNumber(lastTransactionNumber);
		transaction.setAmount(AMOUNT);
		transaction.setInstallments(1);
		transaction.setInstallmentByAdmin(Boolean.FALSE);
		transaction.setUtf8Output(Boolean.TRUE);
		
		return transaction.authorizeFunds();
	}
	
	private Authorization authorizeRegular(AprovaFacilService aprovaFacilService)
			throws ParseException, IOException, JAXBException {
		CreditCard creditCard = new CreditCard();
		creditCard.setNumber("4551870000000183");
		creditCard.setExpirationMonth(9);
		creditCard.setExpirationYear(2014);
		creditCard.setSecurityCode(123);
		creditCard.setBrand(CreditCard.Brand.VISA);
		
		CreditCardHolder holder = new CreditCardHolder();
		holder.setName("Joaquim P Soares");
		holder.setFederalTaxId("12312312300");
		holder.setBirthDate(Util.createDate("20/02/1990"));
		holder.setMotherName("Sebastiana das Couves");
		creditCard.setHolder(holder);
		
		AuthorizationTransaction transaction = new AuthorizationTransaction();
		transaction.setAprovaFacilService(aprovaFacilService);
		
		transaction.setDocumentNumber(ORDER_NUMBER);
		transaction.setAmount(AMOUNT);
		transaction.setCurrency(Currency.BRL);
		transaction.setInstallments(1);
		transaction.setInstallmentByAdmin(Boolean.FALSE);
		transaction.setPreAuthorization(Boolean.FALSE);
		transaction.setCreditCard(creditCard);
		transaction.setAcquirer(Acquirer.CIELO);
		transaction.setBuyerHost("127.0.0.1");
		transaction.setUtf8Output(Boolean.TRUE);
		
		return transaction.authorizeFunds();
	}
	
}