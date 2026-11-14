package net.onefivefour.echolist.core.networking.data.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for HeaderRedactor.
 *
 * Validates: Requirements 3.1, 3.2, 3.3
 */
class HeaderRedactorPropertyTest : FunSpec({

    // Feature: network-request-logging, Property 3: Sensitive header identification
    test("Feature: network-request-logging, Property 3: Sensitive header identification") {
        /**
         * Validates: Requirements 3.2
         *
         * For any header name, the redactor should classify it as sensitive if and only if
         * the name equals "Authorization" (case-insensitive) or contains the substring
         * "token" (case-insensitive). All other header names should be classified as non-sensitive.
         */

        // Generator for sensitive header names: "Authorization" in various cases, or names containing "token"
        val sensitiveNameArb = Arb.element(
            "Authorization",
            "authorization",
            "AUTHORIZATION",
            "AuThOrIzAtIoN",
            "X-Token-Id",
            "x-token-id",
            "X-CSRF-TOKEN",
            "my-token-header",
            "TOKEN",
            "SomeTokenValue"
        )

        // Generator for non-sensitive header names that don't contain "token" or equal "authorization"
        val nonSensitiveNameArb = Arb.string(minSize = 1, maxSize = 30)
            .map { base -> "Header-${base.filter { it.isLetterOrDigit() }}" }
            .map { name ->
                // Ensure the generated name doesn't accidentally match sensitive patterns
                if (name.contains("token", ignoreCase = true) || name.equals("Authorization", ignoreCase = true)) {
                    "X-Safe-Custom"
                } else {
                    name
                }
            }

        // Test sensitive names are identified as sensitive
        checkAll(PropTestConfig(iterations = 100), sensitiveNameArb) { name ->
            val headers = mapOf(name to listOf("some-value"))
            val redacted = HeaderRedactor.redact(headers)
            redacted[name] shouldBe listOf("[REDACTED]")
        }

        // Test non-sensitive names are identified as non-sensitive
        checkAll(PropTestConfig(iterations = 100), nonSensitiveNameArb) { name ->
            val value = listOf("some-value")
            val headers = mapOf(name to value)
            val redacted = HeaderRedactor.redact(headers)
            redacted[name] shouldBe value
        }
    }

    // Feature: network-request-logging, Property 4: Sensitive values never appear in redacted output
    test("Feature: network-request-logging, Property 4: Sensitive values never appear in redacted output") {
        /**
         * Validates: Requirements 3.1, 3.3
         *
         * For any map of headers that includes at least one sensitive header with a non-empty value,
         * after redaction, none of the original sensitive header values should appear anywhere in the
         * redacted map — they should all be replaced with [REDACTED], and all non-sensitive header
         * values should remain unchanged.
         */

        val sensitiveNameArb = Arb.element(
            "Authorization",
            "authorization",
            "X-Token-Id",
            "x-csrf-token",
            "TOKEN"
        )

        val nonSensitiveNameArb = Arb.element(
            "Content-Type",
            "Accept",
            "Cache-Control",
            "X-Request-Id",
            "User-Agent"
        )

        val valueArb = Arb.string(minSize = 1, maxSize = 50)
            .map { it.filter { c -> c.isLetterOrDigit() || c == '-' } }
            .map { if (it.isEmpty()) "val" else it }

        // Use disjoint values: a public header must stay unchanged even if a secret has the same value.
        checkAll(
            PropTestConfig(iterations = 100),
            sensitiveNameArb,
            valueArb.map { "secret-$it" },
            nonSensitiveNameArb,
            valueArb.map { "public-$it" }
        ) { sensName, sensValue, nonSensName, nonSensValue ->
            val headers = mapOf(
                sensName to listOf(sensValue),
                nonSensName to listOf(nonSensValue)
            )

            val redacted = HeaderRedactor.redact(headers)

            // Sensitive header values must be replaced with [REDACTED]
            redacted[sensName] shouldBe listOf("[REDACTED]")

            // Original sensitive value must not appear anywhere in redacted output
            for ((_, values) in redacted) {
                for (v in values) {
                    if (sensValue != "[REDACTED]") {
                        (v == sensValue) shouldBe false
                    }
                }
            }

            // Non-sensitive header values must remain unchanged
            redacted[nonSensName] shouldBe listOf(nonSensValue)
        }
    }
})