package responses;

public class UpdatePostResponse {
    private int status;
    private String error;

    public UpdatePostResponse(int response_code, String error) {
        this.error = error;
        this.status = response_code;

    }

    public int getResponse_code() {
        return status;
    }

    public void setResponse_code(int response_code) {
        this.status = response_code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
