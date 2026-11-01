package com.naisa.util;

import com.naisa.model.vo.ResourceIdResponse;

/**
 * Utility methods for creating resource ID responses.
 *
 * @author James Chen
 */
public final class Resources {
    private Resources() {
    }

    /**
     * Creates a resource ID response using the given object's ID.
     *
     * @param withId the object providing the resource ID
     * @return a response containing the object's ID
     * @author James Chen
     */
    public static ResourceIdResponse createResponseFrom(final WithId withId) {
        final ResourceIdResponse resourceIdResponse = new ResourceIdResponse();
        resourceIdResponse.setId(withId.getId());
        return resourceIdResponse;
    }

    /**
     * Provides access to a resource's {@link Long} ID.
     *
     * @author James Chen
     */
    public interface WithId {
        Long getId();
    }
}
