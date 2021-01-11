package com.force.react;

/**
 * @author Chathura Sarathchandra
 */

public class IPCResponse implements RawResponse {

    /**
     * Raw data from this response.
     */
    public final byte[] data;

    public IPCResponse(byte[] rawdata) {
        data = rawdata;
    }

    @Override
    public byte[] getRawResponse() {
        return data;
    }
}
