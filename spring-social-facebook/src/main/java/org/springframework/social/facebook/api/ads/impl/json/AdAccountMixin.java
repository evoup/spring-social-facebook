package org.springframework.social.facebook.api.ads.impl.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.social.facebook.api.ads.AdAccount.AccountStatus;
import org.springframework.social.facebook.api.ads.AdAccount.AgencyClientDeclaration;
import org.springframework.social.facebook.api.ads.AdAccount.Capabilities;
import org.springframework.social.facebook.api.ads.AdAccount.TaxStatus;
import org.springframework.social.facebook.api.ads.AdAccountGroup;
import org.springframework.social.facebook.api.ads.AdUser;
import org.springframework.social.facebook.api.impl.json.FacebookModule;
import org.springframework.social.facebook.api.impl.json.FacebookObjectMixin;

import java.io.IOException;
import java.util.*;

/**
 * Annotated mixin to add Jackson annotations to AdAccount.
 *
 * @author Sebastian Górecki
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AdAccountMixin extends FacebookObjectMixin {

	@JsonProperty("id")
	String id;

	@JsonProperty("account_groups")
	List<AdAccountGroup> accountGroups;

	@JsonProperty("account_id")
	long accountId;

	@JsonProperty("account_status")
	@JsonDeserialize(using = AccountStatusDeserializer.class)
	AccountStatus status;

	@JsonProperty("age")
	int age;

	@JsonProperty("agency_client_declaration")
	AgencyClientDeclaration agencyClientDeclaration;

	@JsonProperty("amount_spent")
	String amountSpent;

	@JsonProperty("balance")
	String balance;

	@JsonProperty("business_city")
	String businessCity;

	@JsonProperty("business_country_code")
	String businessCountryCode;

	@JsonProperty("business_name")
	String businessName;

	@JsonProperty("business_state")
	String businessState;

	@JsonProperty("business_street")
	String businessStreet;

	@JsonProperty("business_street2")
	String businessStreet2;

	@JsonProperty("business_zip")
	String businessZip;

	@JsonProperty("capabilities")
	@JsonDeserialize(using = CapabilitiesListDeserializer.class)
	List<Capabilities> capabilities;

	@JsonProperty("created_time")
	Date createdTime;

	@JsonProperty("currency")
	String currency;

	@JsonProperty("daily_spend_limit")
	String dailySpendLimit;

	@JsonProperty("end_advertiser")
	long endAdvertiser;

	@JsonProperty("funding_source")
	String fundingSource;

	@JsonProperty("funding_source_details")
	Map<String, Object> fundingSourceDetails;

	@JsonProperty("is_personal")
	int isPersonal;

	@JsonProperty("media_agency")
	long mediaAgency;

	@JsonProperty("name")
	String name;

	@JsonProperty("offsite_pixels_tos_accepted")
	boolean offsitePixelsTOSAccepted;

	@JsonProperty("partner")
	long partner;

	@JsonProperty("spend_cap")
	String spendCap;

	@JsonProperty("timezone_id")
	int timezoneId;

	@JsonProperty("timezone_name")
	String timezoneName;

	@JsonProperty("timezone_offset_hours_utc")
	int timezoneOffsetHoursUTC;

	@JsonProperty("tos_accepted")
	Map<String, Integer> tosAccepted;

	@JsonProperty("users")
	@JsonDeserialize(using = AdUserListDeserializer.class)
	List<AdUser> users;

	@JsonProperty("tax_id_status")
	@JsonDeserialize(using = TaxStatusDeserializer.class)
	TaxStatus taxStatus;

	private static class AccountStatusDeserializer extends JsonDeserializer<AccountStatus> {
		@Override
		public AccountStatus deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			try {
				int status = jp.getIntValue();
				switch (status) {
					case 1:
						return AccountStatus.ACTIVE;
					case 2:
						return AccountStatus.DISABLED;
					case 3:
						return AccountStatus.UNSETTLED;
					case 7:
						return AccountStatus.PENDING_REVIEW;
					case 9:
						return AccountStatus.IN_GRACE_PERIOD;
					case 101:
						return AccountStatus.TEMPORARILY_UNAVAILABLE;
					case 100:
						return AccountStatus.PENDING_CLOSURE;
					default:
						return AccountStatus.UNKNOWN;
				}
			} catch (IOException e) {
				return AccountStatus.UNKNOWN;
			}
		}
	}

	private static class TaxStatusDeserializer extends JsonDeserializer<TaxStatus> {
		@Override
		public TaxStatus deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			try {
				int status = jp.getIntValue();
				switch (status) {
					case 0:
						return TaxStatus.UNKNOWN;
					case 1:
						return TaxStatus.VAT_NOT_REQUIRED_US_CA;
					case 2:
						return TaxStatus.VAT_INFORMATION_REQUIRED;
					case 3:
						return TaxStatus.VAT_INFORMATION_SUBMITTED;
					case 4:
						return TaxStatus.OFFLINE_VAT_VALIDATION_FAILED;
					case 5:
						return TaxStatus.ACCOUNT_IS_PERSONAL_ACCOUNT;
					default:
						return TaxStatus.UNKNOWN;
				}
			} catch (IOException e) {
				return TaxStatus.UNKNOWN;
			}
		}
	}

	private static class AdUserListDeserializer extends JsonDeserializer<List<AdUser>> {
		@SuppressWarnings("unchecked")
		@Override
		public List<AdUser> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new FacebookModule());
			jp.setCodec(mapper);
			if (jp.hasCurrentToken()) {
				try {
					JsonNode dataNode = jp.readValueAs(JsonNode.class).get("data");
					if (dataNode != null) {
						return (List<AdUser>) mapper.reader(new TypeReference<List<AdUser>>() {
						}).readValue(dataNode);
					}
				} catch (IOException e) {
					return Collections.emptyList();
				}
			}

			return Collections.emptyList();
		}
	}

	private static class CapabilitiesListDeserializer extends JsonDeserializer<List<Capabilities>> {
		@Override
		public List<Capabilities> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
				List<Capabilities> capabilities = new ArrayList<Capabilities>();
				try {
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						String capability = jp.getValueAsString();
						try {
							capabilities.add(Capabilities.valueOf(capability.toUpperCase()));
						} catch (IllegalArgumentException e) {
							capabilities.add(Capabilities.UNKNOWN);
						}
					}
					return capabilities;
				} catch (IOException e) {
					return Collections.emptyList();
				}
			}
			return Collections.emptyList();
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public abstract class AgencyClientDeclarationMixin extends FacebookObjectMixin {

		@JsonProperty("agency_representing_client")
		private int agencyRepresentingClient;

		@JsonProperty("client_based_in_france")
		private int clientBasedInFrance;

		@JsonProperty("client_city")
		private String clientCity;

		@JsonProperty("client_country_code")
		private String clientCountryCode;

		@JsonProperty("client_email_address")
		private String clientEmailAddress;

		@JsonProperty("client_name")
		private String clientName;

		@JsonProperty("client_postal_code")
		private String clientPostalCode;

		@JsonProperty("client_province")
		private String clientProvince;

		@JsonProperty("client_street")
		private String clientStreet;

		@JsonProperty("client_street2")
		private String clientStreet2;

		@JsonProperty("has_written_mandate_from_advertiser")
		private int hasWrittenMandateFromAdvertiser;

		@JsonProperty("is_client_paying_invoices")
		private int isClientPayingInvoices;
	}
}
