package heigit.ors.api.errors;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.exceptions.UnknownParameterException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.routing.RoutingErrorCodes;
import heigit.ors.util.AppInfo;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    protected static Logger LOGGER = Logger.getLogger(RestResponseEntityExceptionHandler.class.getName());

    @ExceptionHandler(value = InvalidDefinitionException.class)
    public ResponseEntity handleInvalidDefinitionException(InvalidDefinitionException exception) {
        return handleStatusCodeException((StatusCodeException) exception.getCause());
    }

    @ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Object> handleMimeError(final HttpMediaTypeNotAcceptableException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(LOGGER.isDebugEnabled()) {
            // Log also the stack trace
            LOGGER.error("Exception", exception);
        } else {
            // Log only the error message
            LOGGER.error(exception);
        }

        return new ResponseEntity(constructErrorBody(new UnknownParameterValueException(RoutingErrorCodes.EXPORT_HANDLER_ERROR, "mime-type", "")), headers, HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(value = StatusCodeException.class)
    public ResponseEntity handleStatusCodeException(StatusCodeException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(LOGGER.isDebugEnabled()) {
            // Log also the stack trace
            LOGGER.error("Exception", exception);
        } else {
            // Log only the error message
            LOGGER.error(exception);
        }

        return new ResponseEntity(constructErrorBody(exception), headers, convertStatus(exception.getStatusCode()));
    }

    @ExceptionHandler(value = UnknownParameterException.class)
    public ResponseEntity handleUnknownParameterException(UnknownParameterException exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(LOGGER.isDebugEnabled()) {
            // Log also the stack trace
            LOGGER.error("Exception", exception);
        } else {
            // Log only the error message
            LOGGER.error(exception);
        }

        return new ResponseEntity(constructErrorBody(exception), headers, convertStatus(exception.getStatusCode()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity handleGenericException(Exception exception) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if(LOGGER.isDebugEnabled()) {
            // Log also the stack trace
            LOGGER.error("Exception", exception);
        } else {
            // Log only the error message
            LOGGER.error(exception);
        }

        Throwable cause = exception.getCause();
        if(cause instanceof InvalidDefinitionException) {
            InvalidDefinitionException e = (InvalidDefinitionException) cause;
            if(e.getCause() instanceof StatusCodeException) {
                StatusCodeException origExc = (StatusCodeException) e.getCause();
                return new ResponseEntity(constructErrorBody(origExc), headers, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity(constructErrorBody(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "")), headers, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(constructGenericErrorBody(exception), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus convertStatus(int statusCode) {
        return HttpStatus.valueOf(statusCode);
    }

    private String constructErrorBody(StatusCodeException exception) {
        int errorCode = -1;

        errorCode = exception.getInternalCode();

        JSONObject json = constructJsonBody(exception, errorCode);

        return json.toString();
    }

    private JSONObject constructJsonBody(Exception exception, int internalErrorCode) {
        JSONObject json = new JSONObject();

        JSONObject jError = new JSONObject();
        jError.put("message", exception.getMessage());

        if(internalErrorCode != -1) {
            jError.put("code", internalErrorCode);
        }

        json.put("error", jError);

        JSONObject jInfo = new JSONObject();
        jInfo.put("engine", AppInfo.getEngineInfo());
        jInfo.put("timestamp", System.currentTimeMillis());
        json.put("info", jInfo);

        return json;
    }

    private String constructGenericErrorBody(Exception exception) {
        return constructJsonBody(exception, -1).toString();
    }
}