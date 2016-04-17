package com.villarsolutions.primordial.health;

import com.codahale.metrics.health.HealthCheck;

/**
 * Dropwizard HealthCheck class - required for all DropWizard applications.
 * <p>
 * This is not yet implemented, but it could be written to contain ad-hoc
 * health-checks that verify the health of the application server.
 */
public class DefaultHealthCheck extends HealthCheck {

    public DefaultHealthCheck() {
    }

    /**
     * Can add health-checks here if necessary.
     */
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
