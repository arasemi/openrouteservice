package heigit.ors.servlet.filters;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

import org.meteogroup.jbrotli.Brotli;
import org.meteogroup.jbrotli.io.BrotliOutputStream;
import org.meteogroup.jbrotli.libloader.BrotliLibraryLoader;

import heigit.ors.io.ByteArrayOutputStreamEx;

class BrotliResponseStream extends ServletOutputStream { 
	private ByteArrayOutputStreamEx _bufferStream = null;
	private BrotliOutputStream _brotliStream = null;
	private ServletOutputStream _outputStream = null;
	private HttpServletResponse _response = null;
	private boolean _closed = false;
	
	static 
	{
		 BrotliLibraryLoader.loadBrotli();
	}

	public BrotliResponseStream(HttpServletResponse response) throws IOException {
		super();
		
		_response = response;
		_outputStream = response.getOutputStream();
		_bufferStream = new ByteArrayOutputStreamEx();
		_brotliStream = new BrotliOutputStream(_bufferStream, Brotli.DEFAULT_PARAMETER);
	}

	public void close() throws IOException { 
		if (_closed) 
			throw new IOException("This output stream has already been closed");

		_brotliStream.flush();
		
		byte[] bytes = _bufferStream.getBuffer();
		int bytesLength = _bufferStream.size() - 1;

		_response.setContentLength(bytesLength); 
        _response.addHeader("Content-Encoding", ContentEncodingType.BROTLI);

        _outputStream.write(bytes, 0, bytesLength); 

		_brotliStream.close();
        _outputStream.close();
		_closed = true;
	}
	
	public boolean isClosed() {
		return _closed;
	}

	public void flush() throws IOException {
		if (_closed) 
			throw new IOException("Cannot flush a closed output stream");
		
		_brotliStream.flush();
	}

	public void write(int b) throws IOException {
		if (_closed) 
			throw new IOException("Cannot write to a closed output stream");
		
		_brotliStream.write((byte)b);
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (_closed) 
			throw new IOException("Cannot write to a closed output stream");
		
		_brotliStream.write(b, off, len);
	}

	public void reset() {

	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void setWriteListener(WriteListener arg0) {
	}
}