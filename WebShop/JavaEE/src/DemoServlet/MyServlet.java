package DemoServlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(description = "MySimpleConnectionCnt", urlPatterns = { "/ConnectionCount" })
public class MyServlet extends HttpServlet {

	int connectionCnt = 0;
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		connectionCnt++;
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		connectionCnt =0;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		System.out.println(connectionCnt++);
	}


}

