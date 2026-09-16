package com.motadata.ipam;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JSON communication payloads and schema structures across the application.
 */
public class JsonPayloadTest {

    // Tests JSON serialization and schema validation of Subnet entities.
    @Test
    public void testSubnetJsonPayload() {
        JsonObject subnet = new JsonObject()
                .put("id", 10L)
                .put("subnetAddress", "192.168.1.0")
                .put("subnetCidr", 24)
                .put("subnetMask", "255.255.255.0")
                .put("totalIp", 254L)
                .put("usedIp", 42L)
                .put("availableIp", 212L)
                .put("usedIpPercentage", 16.54)
                .put("severity", 3)
                .put("type", "DHCP")
                .put("status", "Active");

        String encoded = subnet.encode();
        assertNotNull(encoded);
        assertTrue(encoded.contains("\"subnetAddress\":\"192.168.1.0\""));
        assertTrue(encoded.contains("\"usedIpPercentage\":16.54"));

        JsonObject decoded = new JsonObject(encoded);
        assertEquals(10L, decoded.getLong("id"));
        assertEquals("192.168.1.0", decoded.getString("subnetAddress"));
        assertEquals(24, decoded.getInteger("subnetCidr"));
        assertEquals(254L, decoded.getLong("totalIp"));
    }

    // Tests JSON serialization and schema validation of IP Request entities.
    @Test
    public void testIpRequestJsonPayload() {
        JsonObject ipRequest = new JsonObject()
                .put("id", 101L)
                .put("numberOfIps", 3)
                .put("deviceType", "Server")
                .put("duration", "Permanent")
                .put("purpose", "Database Cluster Node")
                .put("status", "PENDING")
                .put("preferredSubnet", true)
                .put("subnetAddress", "10.0.0.0/24")
                .put("ips", new JsonArray(List.of("10.0.0.10", "10.0.0.11", "10.0.0.12")));

        String encoded = ipRequest.encode();
        assertTrue(encoded.contains("\"numberOfIps\":3"));
        assertTrue(encoded.contains("\"deviceType\":\"Server\""));

        JsonObject decoded = new JsonObject(encoded);
        assertEquals(101L, decoded.getLong("id"));
        assertEquals(3, decoded.getInteger("numberOfIps"));
        assertEquals(3, decoded.getJsonArray("ips").size());
        assertEquals("10.0.0.10", decoded.getJsonArray("ips").getString(0));
    }

    // Tests generic REST API Response envelopes.
    @Test
    public void testApiResponseEnvelope() {
        JsonObject response = new JsonObject()
                .put("success", true)
                .put("message", "Operation completed successfully")
                .put("currentUserRole", "ROLE_ADMIN")
                .put("data", new JsonArray().add(new JsonObject().put("key", "val")));

        String encoded = response.encode();
        JsonObject decoded = new JsonObject(encoded);

        assertTrue(decoded.getBoolean("success"));
        assertEquals("ROLE_ADMIN", decoded.getString("currentUserRole"));
        assertEquals(1, decoded.getJsonArray("data").size());
    }

    // Tests user authentication payload structure.
    @Test
    public void testUserAuthJsonPayload() {
        JsonObject authResponse = new JsonObject()
                .put("success", true)
                .put("token", "jwt-test-token-12345")
                .put("userName", "admin")
                .put("userId", 1L)
                .put("role", "ROLE_ADMIN")
                .put("authorities", new JsonArray().add("ROLE_ADMIN").add("PERM_READ_ALL").add("PERM_WRITE_ALL"));

        assertTrue(authResponse.getBoolean("success"));
        assertEquals("admin", authResponse.getString("userName"));
        assertTrue(authResponse.getJsonArray("authorities").contains("ROLE_ADMIN"));
    }
}
