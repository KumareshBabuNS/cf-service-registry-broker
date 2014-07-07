package com.mattstine.cf.srb.controller;

import com.jayway.restassured.http.ContentType;
import com.mattstine.cf.srb.model.*;
import com.mattstine.cf.srb.repository.ServiceBindingRepository;
import com.mattstine.cf.srb.repository.ServiceInstanceRepository;
import com.mattstine.cf.srb.repository.ServiceRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.app.ApplicationInstanceInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.Mockito.*;

public class ServiceBrokerControllerTest {

    private ServiceBrokerController serviceBrokerController;
    private ServiceInstanceRepository serviceInstanceRepository;
    private ServiceBindingRepository serviceBindingRepository;

    @Before
    public void setUp() {
        ApplicationInstanceInfo applicationInstanceInfo = new ApplicationInstanceInfo() {
            @Override
            public String getInstanceId() {
                return null;
            }

            @Override
            public String getAppId() {
                return null;
            }

            @Override
            public Map<String, Object> getProperties() {
                Map<String, Object> properties = new HashMap();
                properties.put("uris", Arrays.asList("my.uri.com"));
                return properties;
            }
        };

        Cloud cloud = mock(Cloud.class);
        when(cloud.getApplicationInstanceInfo()).thenReturn(applicationInstanceInfo);

        Service service = new Service();
        service.setId("123-456-789");
        service.setName("HaaSh");
        service.setBindable(true);
        service.setDescription("HashMap as a Service");

        Plan plan = new Plan();
        plan.setId("123-456-789");
        plan.setName("Basic");
        plan.setDescription("Basic Plan");
        service.addPlan(plan);

        List services = Arrays.asList(service);

        ServiceRepository serviceRepository = mock(ServiceRepository.class);
        when(serviceRepository.findAll()).thenReturn(services);

        serviceInstanceRepository = mock(ServiceInstanceRepository.class);
        serviceBindingRepository = mock(ServiceBindingRepository.class);

        serviceBrokerController = new ServiceBrokerController(
                cloud,
                serviceRepository,
                serviceInstanceRepository,
                serviceBindingRepository);
    }

    @Test
    public void catalogTest() {
        given()
                .standaloneSetup(serviceBrokerController)
                .when()
                .get("/v2/catalog").
                then()
                .statusCode(200)
                .body("services.id", hasItems("123-456-789"))
                .body("services.bindable", hasItems(true))
                .body("services.plans.id", hasItems(Arrays.asList("123-456-789")))
                .body("services.plans.name", hasItems(Arrays.asList("Basic")));
    }

    @Test
    public void createTest() {
        ServiceInstance requestBody = new ServiceInstance();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setOrganizationGuid("12345");
        requestBody.setSpaceGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345")
                .then()
                .statusCode(201)
                .assertThat().body(equalTo("{}"));

        verify(serviceInstanceRepository).exists("12345");

        ServiceInstance newServiceInstance = new ServiceInstance();
        newServiceInstance.setId("12345");
        newServiceInstance.setServiceId("12345");
        newServiceInstance.setPlanId("12345");
        newServiceInstance.setOrganizationGuid("12345");
        newServiceInstance.setSpaceGuid("12345");

        verify(serviceInstanceRepository).save(newServiceInstance);
    }

    @Test
    public void createWithConflict() {
        ServiceInstance existingServiceInstance = new ServiceInstance();
        existingServiceInstance.setId("12345");
        existingServiceInstance.setServiceId("12345");
        existingServiceInstance.setPlanId("12345");
        existingServiceInstance.setOrganizationGuid("12345");
        existingServiceInstance.setSpaceGuid("something different");

        when(serviceInstanceRepository.exists("12345")).thenReturn(true);
        when(serviceInstanceRepository.findOne("12345")).thenReturn(existingServiceInstance);

        ServiceInstance requestBody = new ServiceInstance();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setOrganizationGuid("12345");
        requestBody.setSpaceGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345")
                .then()
                .statusCode(409)
                .assertThat().body(equalTo("{}"));
    }

    @Test
    public void createWithCopy() {
        ServiceInstance existingServiceInstance = new ServiceInstance();
        existingServiceInstance.setId("12345");
        existingServiceInstance.setServiceId("12345");
        existingServiceInstance.setPlanId("12345");
        existingServiceInstance.setOrganizationGuid("12345");
        existingServiceInstance.setSpaceGuid("12345");

        when(serviceInstanceRepository.exists("12345")).thenReturn(true);
        when(serviceInstanceRepository.findOne("12345")).thenReturn(existingServiceInstance);

        ServiceInstance requestBody = new ServiceInstance();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setOrganizationGuid("12345");
        requestBody.setSpaceGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345")
                .then()
                .statusCode(200)
                .assertThat().body(equalTo("{}"));
    }

    @Test
    public void createBinding() {
        ServiceBinding requestBody = new ServiceBinding();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setAppGuid("12345");

        when(serviceInstanceRepository.exists("12345")).thenReturn(true);
        when(serviceBindingRepository.exists("12345")).thenReturn(false);

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345/service_bindings/12345")
                .then()
                .statusCode(201)
                .assertThat().body("credentials.uri", equalTo("http://my.uri.com/HaaSh/12345"))
                .body("credentials.username", equalTo("warreng"))
                .body("credentials.password", equalTo("natedogg"));

        Credentials credentials = new Credentials();
        credentials.setUri("http://my.uri.com/HaaSh/12345");
        credentials.setUsername("warreng");
        credentials.setPassword("natedogg");

        ServiceBinding newServiceBinding = new ServiceBinding();
        newServiceBinding.setId("12345");
        newServiceBinding.setInstanceId("12345");
        newServiceBinding.setServiceId("12345");
        newServiceBinding.setPlanId("12345");
        newServiceBinding.setAppGuid("12345");
        newServiceBinding.setCredentials(credentials);

        verify(serviceBindingRepository).save(newServiceBinding);
    }
}
