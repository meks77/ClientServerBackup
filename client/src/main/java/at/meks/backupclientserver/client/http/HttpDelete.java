package at.meks.backupclientserver.client.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

class HttpDelete extends HttpEntityEnclosingRequestBase {

    private static final String DELETE = "DELETE";

    HttpDelete(String url) {
        super();
        setURI(URI.create(url));
    }

    @Override
    public String getMethod() {
        return DELETE;
    }
}
