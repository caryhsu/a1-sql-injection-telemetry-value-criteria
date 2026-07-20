package org.tasa.socs.moc.hdtrend.config;

import org.springframework.context.annotation.Configuration;

/**
 * REPRO-ONLY marker — kept intentionally empty.
 *
 * <p>JSON binding for {@code TelemetryValueCriterion} is enabled build-side via the
 * project-root {@code lombok.config} ({@code lombok.anyConstructor.addConstructorProperties = true}),
 * which annotates Lombok constructors with {@code @ConstructorProperties} so any
 * Jackson version can use them as property-based creators. No production class is
 * modified; nothing here is part of the taint chain.
 */
@Configuration
public class ReproJacksonConfig {
}
