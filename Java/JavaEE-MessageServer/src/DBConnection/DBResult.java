package DBConnection;

public class DBResult<T> {
	
	public T result;
	public boolean success;
	
	public DBResult(T result, boolean success){
		this.success=success;
		this.result=result;
	}
}
