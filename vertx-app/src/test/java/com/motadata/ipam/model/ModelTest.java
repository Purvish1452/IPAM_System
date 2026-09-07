package com.motadata.ipam.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testUserJsonSerialization() throws Exception {
        User user = new User(1L, "admin", "admin@motadata.com", true);
        UserRole role = new UserRole(1L, "ROLE_ADMIN", "Administrator");
        user.setUserRoleId(role);

        String json = objectMapper.writeValueAsString(user);
        assertTrue(json.contains("\"userName\":\"admin\""));
        assertTrue(json.contains("\"role\":\"ROLE_ADMIN\""));

        User deserialized = objectMapper.readValue(json, User.class);
        assertEquals(user.getId(), deserialized.getId());
        assertEquals(user.getUserName(), deserialized.getUserName());
        assertNotNull(deserialized.getUserRoleId());
        assertEquals("ROLE_ADMIN", deserialized.getUserRoleId().getRole());
    }

    @Test
    public void testSubnetDetailsJsonSerialization() throws Exception {
        SubnetDetails subnet = new SubnetDetails(10L, "192.168.1.0", "255.255.255.0");
        subnet.setDescription("Office Subnet");

        String json = objectMapper.writeValueAsString(subnet);
        assertTrue(json.contains("\"subnetAddress\":\"192.168.1.0\""));

        SubnetDetails deserialized = objectMapper.readValue(json, SubnetDetails.class);
        assertEquals("192.168.1.0", deserialized.getSubnetAddress());
        assertEquals("255.255.255.0", deserialized.getSubnetMask());
    }

    @Test
    public void testResponseWrapperSerialization() throws Exception {
        Response response = new Response(new User(1L, "admin", "admin@motadata.com", true), true, "Success");
        response.setCurrentUserRole("ROLE_ADMIN");

        String json = objectMapper.writeValueAsString(response);
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"currentUserRole\":\"ROLE_ADMIN\""));
    }

    @Test
    public void testIpRequestJsonSerialization() throws Exception {
        IpRequest req = new IpRequest();
        req.setId(101L);
        req.setNumberOfIps(3);
        req.setCreatedBy("engineer");
        req.setDeviceType("Server");
        req.setDuration("Permanent");
        req.setPurpose("Database Cluster Node");
        req.setStatus("PENDING");
        req.setPreferredSubnet(true);
        req.setSubnetId("2");
        req.setSubnetAddress("10.0.0.0/24");
        req.setIps(java.util.Arrays.asList("10.0.0.10", "10.0.0.11", "10.0.0.12"));

        String json = objectMapper.writeValueAsString(req);
        assertTrue(json.contains("\"numberOfIps\":3"));
        assertTrue(json.contains("\"deviceType\":\"Server\""));
        assertTrue(json.contains("\"duration\":\"Permanent\""));
        assertTrue(json.contains("\"purpose\":\"Database Cluster Node\""));
        assertTrue(json.contains("\"10.0.0.10\""));

        IpRequest deserialized = objectMapper.readValue(json, IpRequest.class);
        assertEquals(101L, deserialized.getId());
        assertEquals(3, deserialized.getNumberOfIps());
        assertEquals("Server", deserialized.getDeviceType());
        assertEquals("Permanent", deserialized.getDuration());
        assertEquals(3, deserialized.getIps().size());
    }
}
