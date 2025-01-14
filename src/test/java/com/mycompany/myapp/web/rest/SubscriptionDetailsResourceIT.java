package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.SubscriptionDetailsAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.SubscriptionDetails;
import com.mycompany.myapp.repository.EntityManager;
import com.mycompany.myapp.repository.SubscriptionDetailsRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link SubscriptionDetailsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class SubscriptionDetailsResourceIT {

    private static final String DEFAULT_SUBSCRIPTION_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SUBSCRIPTION_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_SUBSCRIPTION_AMOUNT = 1;
    private static final Integer UPDATED_SUBSCRIPTION_AMOUNT = 2;

    private static final Integer DEFAULT_TAX_AMOUNT = 1;
    private static final Integer UPDATED_TAX_AMOUNT = 2;

    private static final Integer DEFAULT_TOTAL_AMOUNT = 1;
    private static final Integer UPDATED_TOTAL_AMOUNT = 2;

    private static final Instant DEFAULT_SUBSCRIPTION_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SUBSCRIPTION_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_SUBSCRIPTION_EXPIRY_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SUBSCRIPTION_EXPIRY_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_ADDITIONAL_COMMENTS = "AAAAAAAAAA";
    private static final String UPDATED_ADDITIONAL_COMMENTS = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final Integer DEFAULT_NOTIFICATION_BEFORE_EXPIRY = 1;
    private static final Integer UPDATED_NOTIFICATION_BEFORE_EXPIRY = 2;

    private static final Boolean DEFAULT_NOTIFICATION_MUTE_FLAG = false;
    private static final Boolean UPDATED_NOTIFICATION_MUTE_FLAG = true;

    private static final String DEFAULT_NOTIFICATION_TO = "AAAAAAAAAA";
    private static final String UPDATED_NOTIFICATION_TO = "BBBBBBBBBB";

    private static final String DEFAULT_NOTIFICATION_CC = "AAAAAAAAAA";
    private static final String UPDATED_NOTIFICATION_CC = "BBBBBBBBBB";

    private static final String DEFAULT_NOTIFICATION_BCC = "AAAAAAAAAA";
    private static final String UPDATED_NOTIFICATION_BCC = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/subscription-details";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SubscriptionDetailsRepository subscriptionDetailsRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private SubscriptionDetails subscriptionDetails;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SubscriptionDetails createEntity(EntityManager em) {
        SubscriptionDetails subscriptionDetails = new SubscriptionDetails()
            .subscriptionName(DEFAULT_SUBSCRIPTION_NAME)
            .subscriptionAmount(DEFAULT_SUBSCRIPTION_AMOUNT)
            .taxAmount(DEFAULT_TAX_AMOUNT)
            .totalAmount(DEFAULT_TOTAL_AMOUNT)
            .subscriptionStartDate(DEFAULT_SUBSCRIPTION_START_DATE)
            .subscriptionExpiryDate(DEFAULT_SUBSCRIPTION_EXPIRY_DATE)
            .additionalComments(DEFAULT_ADDITIONAL_COMMENTS)
            .category(DEFAULT_CATEGORY)
            .notificationBeforeExpiry(DEFAULT_NOTIFICATION_BEFORE_EXPIRY)
            .notificationMuteFlag(DEFAULT_NOTIFICATION_MUTE_FLAG)
            .notificationTo(DEFAULT_NOTIFICATION_TO)
            .notificationCc(DEFAULT_NOTIFICATION_CC)
            .notificationBcc(DEFAULT_NOTIFICATION_BCC);
        return subscriptionDetails;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SubscriptionDetails createUpdatedEntity(EntityManager em) {
        SubscriptionDetails subscriptionDetails = new SubscriptionDetails()
            .subscriptionName(UPDATED_SUBSCRIPTION_NAME)
            .subscriptionAmount(UPDATED_SUBSCRIPTION_AMOUNT)
            .taxAmount(UPDATED_TAX_AMOUNT)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .subscriptionStartDate(UPDATED_SUBSCRIPTION_START_DATE)
            .subscriptionExpiryDate(UPDATED_SUBSCRIPTION_EXPIRY_DATE)
            .additionalComments(UPDATED_ADDITIONAL_COMMENTS)
            .category(UPDATED_CATEGORY)
            .notificationBeforeExpiry(UPDATED_NOTIFICATION_BEFORE_EXPIRY)
            .notificationMuteFlag(UPDATED_NOTIFICATION_MUTE_FLAG)
            .notificationTo(UPDATED_NOTIFICATION_TO)
            .notificationCc(UPDATED_NOTIFICATION_CC)
            .notificationBcc(UPDATED_NOTIFICATION_BCC);
        return subscriptionDetails;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(SubscriptionDetails.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        subscriptionDetails = createEntity(em);
    }

    @Test
    void createSubscriptionDetails() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the SubscriptionDetails
        var returnedSubscriptionDetails = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(SubscriptionDetails.class)
            .returnResult()
            .getResponseBody();

        // Validate the SubscriptionDetails in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSubscriptionDetailsUpdatableFieldsEquals(
            returnedSubscriptionDetails,
            getPersistedSubscriptionDetails(returnedSubscriptionDetails)
        );
    }

    @Test
    void createSubscriptionDetailsWithExistingId() throws Exception {
        // Create the SubscriptionDetails with an existing ID
        subscriptionDetails.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SubscriptionDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkSubscriptionNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setSubscriptionName(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkSubscriptionAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setSubscriptionAmount(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkTaxAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setTaxAmount(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkTotalAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setTotalAmount(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkSubscriptionStartDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setSubscriptionStartDate(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkSubscriptionExpiryDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setSubscriptionExpiryDate(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkAdditionalCommentsIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setAdditionalComments(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCategoryIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setCategory(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkNotificationBeforeExpiryIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setNotificationBeforeExpiry(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkNotificationMuteFlagIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setNotificationMuteFlag(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkNotificationToIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        subscriptionDetails.setNotificationTo(null);

        // Create the SubscriptionDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllSubscriptionDetailsAsStream() {
        // Initialize the database
        subscriptionDetailsRepository.save(subscriptionDetails).block();

        List<SubscriptionDetails> subscriptionDetailsList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(SubscriptionDetails.class)
            .getResponseBody()
            .filter(subscriptionDetails::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(subscriptionDetailsList).isNotNull();
        assertThat(subscriptionDetailsList).hasSize(1);
        SubscriptionDetails testSubscriptionDetails = subscriptionDetailsList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertSubscriptionDetailsAllPropertiesEquals(subscriptionDetails, testSubscriptionDetails);
        assertSubscriptionDetailsUpdatableFieldsEquals(subscriptionDetails, testSubscriptionDetails);
    }

    @Test
    void getAllSubscriptionDetails() {
        // Initialize the database
        subscriptionDetailsRepository.save(subscriptionDetails).block();

        // Get all the subscriptionDetailsList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(subscriptionDetails.getId().intValue()))
            .jsonPath("$.[*].subscriptionName")
            .value(hasItem(DEFAULT_SUBSCRIPTION_NAME))
            .jsonPath("$.[*].subscriptionAmount")
            .value(hasItem(DEFAULT_SUBSCRIPTION_AMOUNT))
            .jsonPath("$.[*].taxAmount")
            .value(hasItem(DEFAULT_TAX_AMOUNT))
            .jsonPath("$.[*].totalAmount")
            .value(hasItem(DEFAULT_TOTAL_AMOUNT))
            .jsonPath("$.[*].subscriptionStartDate")
            .value(hasItem(DEFAULT_SUBSCRIPTION_START_DATE.toString()))
            .jsonPath("$.[*].subscriptionExpiryDate")
            .value(hasItem(DEFAULT_SUBSCRIPTION_EXPIRY_DATE.toString()))
            .jsonPath("$.[*].additionalComments")
            .value(hasItem(DEFAULT_ADDITIONAL_COMMENTS))
            .jsonPath("$.[*].category")
            .value(hasItem(DEFAULT_CATEGORY))
            .jsonPath("$.[*].notificationBeforeExpiry")
            .value(hasItem(DEFAULT_NOTIFICATION_BEFORE_EXPIRY))
            .jsonPath("$.[*].notificationMuteFlag")
            .value(hasItem(DEFAULT_NOTIFICATION_MUTE_FLAG.booleanValue()))
            .jsonPath("$.[*].notificationTo")
            .value(hasItem(DEFAULT_NOTIFICATION_TO))
            .jsonPath("$.[*].notificationCc")
            .value(hasItem(DEFAULT_NOTIFICATION_CC))
            .jsonPath("$.[*].notificationBcc")
            .value(hasItem(DEFAULT_NOTIFICATION_BCC));
    }

    @Test
    void getSubscriptionDetails() {
        // Initialize the database
        subscriptionDetailsRepository.save(subscriptionDetails).block();

        // Get the subscriptionDetails
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, subscriptionDetails.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(subscriptionDetails.getId().intValue()))
            .jsonPath("$.subscriptionName")
            .value(is(DEFAULT_SUBSCRIPTION_NAME))
            .jsonPath("$.subscriptionAmount")
            .value(is(DEFAULT_SUBSCRIPTION_AMOUNT))
            .jsonPath("$.taxAmount")
            .value(is(DEFAULT_TAX_AMOUNT))
            .jsonPath("$.totalAmount")
            .value(is(DEFAULT_TOTAL_AMOUNT))
            .jsonPath("$.subscriptionStartDate")
            .value(is(DEFAULT_SUBSCRIPTION_START_DATE.toString()))
            .jsonPath("$.subscriptionExpiryDate")
            .value(is(DEFAULT_SUBSCRIPTION_EXPIRY_DATE.toString()))
            .jsonPath("$.additionalComments")
            .value(is(DEFAULT_ADDITIONAL_COMMENTS))
            .jsonPath("$.category")
            .value(is(DEFAULT_CATEGORY))
            .jsonPath("$.notificationBeforeExpiry")
            .value(is(DEFAULT_NOTIFICATION_BEFORE_EXPIRY))
            .jsonPath("$.notificationMuteFlag")
            .value(is(DEFAULT_NOTIFICATION_MUTE_FLAG.booleanValue()))
            .jsonPath("$.notificationTo")
            .value(is(DEFAULT_NOTIFICATION_TO))
            .jsonPath("$.notificationCc")
            .value(is(DEFAULT_NOTIFICATION_CC))
            .jsonPath("$.notificationBcc")
            .value(is(DEFAULT_NOTIFICATION_BCC));
    }

    @Test
    void getNonExistingSubscriptionDetails() {
        // Get the subscriptionDetails
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingSubscriptionDetails() throws Exception {
        // Initialize the database
        subscriptionDetailsRepository.save(subscriptionDetails).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the subscriptionDetails
        SubscriptionDetails updatedSubscriptionDetails = subscriptionDetailsRepository.findById(subscriptionDetails.getId()).block();
        updatedSubscriptionDetails
            .subscriptionName(UPDATED_SUBSCRIPTION_NAME)
            .subscriptionAmount(UPDATED_SUBSCRIPTION_AMOUNT)
            .taxAmount(UPDATED_TAX_AMOUNT)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .subscriptionStartDate(UPDATED_SUBSCRIPTION_START_DATE)
            .subscriptionExpiryDate(UPDATED_SUBSCRIPTION_EXPIRY_DATE)
            .additionalComments(UPDATED_ADDITIONAL_COMMENTS)
            .category(UPDATED_CATEGORY)
            .notificationBeforeExpiry(UPDATED_NOTIFICATION_BEFORE_EXPIRY)
            .notificationMuteFlag(UPDATED_NOTIFICATION_MUTE_FLAG)
            .notificationTo(UPDATED_NOTIFICATION_TO)
            .notificationCc(UPDATED_NOTIFICATION_CC)
            .notificationBcc(UPDATED_NOTIFICATION_BCC);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedSubscriptionDetails.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedSubscriptionDetails))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the SubscriptionDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSubscriptionDetailsToMatchAllProperties(updatedSubscriptionDetails);
    }

    @Test
    void putNonExistingSubscriptionDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionDetails.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, subscriptionDetails.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SubscriptionDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchSubscriptionDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionDetails.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SubscriptionDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamSubscriptionDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionDetails.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the SubscriptionDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateSubscriptionDetailsWithPatch() throws Exception {
        // Initialize the database
        subscriptionDetailsRepository.save(subscriptionDetails).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the subscriptionDetails using partial update
        SubscriptionDetails partialUpdatedSubscriptionDetails = new SubscriptionDetails();
        partialUpdatedSubscriptionDetails.setId(subscriptionDetails.getId());

        partialUpdatedSubscriptionDetails
            .subscriptionName(UPDATED_SUBSCRIPTION_NAME)
            .subscriptionAmount(UPDATED_SUBSCRIPTION_AMOUNT)
            .taxAmount(UPDATED_TAX_AMOUNT)
            .subscriptionStartDate(UPDATED_SUBSCRIPTION_START_DATE)
            .subscriptionExpiryDate(UPDATED_SUBSCRIPTION_EXPIRY_DATE)
            .additionalComments(UPDATED_ADDITIONAL_COMMENTS)
            .notificationMuteFlag(UPDATED_NOTIFICATION_MUTE_FLAG)
            .notificationBcc(UPDATED_NOTIFICATION_BCC);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSubscriptionDetails.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedSubscriptionDetails))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the SubscriptionDetails in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSubscriptionDetailsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSubscriptionDetails, subscriptionDetails),
            getPersistedSubscriptionDetails(subscriptionDetails)
        );
    }

    @Test
    void fullUpdateSubscriptionDetailsWithPatch() throws Exception {
        // Initialize the database
        subscriptionDetailsRepository.save(subscriptionDetails).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the subscriptionDetails using partial update
        SubscriptionDetails partialUpdatedSubscriptionDetails = new SubscriptionDetails();
        partialUpdatedSubscriptionDetails.setId(subscriptionDetails.getId());

        partialUpdatedSubscriptionDetails
            .subscriptionName(UPDATED_SUBSCRIPTION_NAME)
            .subscriptionAmount(UPDATED_SUBSCRIPTION_AMOUNT)
            .taxAmount(UPDATED_TAX_AMOUNT)
            .totalAmount(UPDATED_TOTAL_AMOUNT)
            .subscriptionStartDate(UPDATED_SUBSCRIPTION_START_DATE)
            .subscriptionExpiryDate(UPDATED_SUBSCRIPTION_EXPIRY_DATE)
            .additionalComments(UPDATED_ADDITIONAL_COMMENTS)
            .category(UPDATED_CATEGORY)
            .notificationBeforeExpiry(UPDATED_NOTIFICATION_BEFORE_EXPIRY)
            .notificationMuteFlag(UPDATED_NOTIFICATION_MUTE_FLAG)
            .notificationTo(UPDATED_NOTIFICATION_TO)
            .notificationCc(UPDATED_NOTIFICATION_CC)
            .notificationBcc(UPDATED_NOTIFICATION_BCC);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSubscriptionDetails.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedSubscriptionDetails))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the SubscriptionDetails in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSubscriptionDetailsUpdatableFieldsEquals(
            partialUpdatedSubscriptionDetails,
            getPersistedSubscriptionDetails(partialUpdatedSubscriptionDetails)
        );
    }

    @Test
    void patchNonExistingSubscriptionDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionDetails.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, subscriptionDetails.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SubscriptionDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchSubscriptionDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionDetails.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SubscriptionDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamSubscriptionDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        subscriptionDetails.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(subscriptionDetails))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the SubscriptionDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteSubscriptionDetails() {
        // Initialize the database
        subscriptionDetailsRepository.save(subscriptionDetails).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the subscriptionDetails
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, subscriptionDetails.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return subscriptionDetailsRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected SubscriptionDetails getPersistedSubscriptionDetails(SubscriptionDetails subscriptionDetails) {
        return subscriptionDetailsRepository.findById(subscriptionDetails.getId()).block();
    }

    protected void assertPersistedSubscriptionDetailsToMatchAllProperties(SubscriptionDetails expectedSubscriptionDetails) {
        // Test fails because reactive api returns an empty object instead of null
        // assertSubscriptionDetailsAllPropertiesEquals(expectedSubscriptionDetails, getPersistedSubscriptionDetails(expectedSubscriptionDetails));
        assertSubscriptionDetailsUpdatableFieldsEquals(
            expectedSubscriptionDetails,
            getPersistedSubscriptionDetails(expectedSubscriptionDetails)
        );
    }

    protected void assertPersistedSubscriptionDetailsToMatchUpdatableProperties(SubscriptionDetails expectedSubscriptionDetails) {
        // Test fails because reactive api returns an empty object instead of null
        // assertSubscriptionDetailsAllUpdatablePropertiesEquals(expectedSubscriptionDetails, getPersistedSubscriptionDetails(expectedSubscriptionDetails));
        assertSubscriptionDetailsUpdatableFieldsEquals(
            expectedSubscriptionDetails,
            getPersistedSubscriptionDetails(expectedSubscriptionDetails)
        );
    }
}
