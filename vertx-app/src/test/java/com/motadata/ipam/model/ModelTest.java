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
}
