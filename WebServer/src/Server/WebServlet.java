package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class WebServlet
 */

public class WebServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//private TransServer ts;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebServlet() {
        super();
        //ts = new TransServer();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*PrintWriter out = response.getWriter();
		String input = request.getParameter("param0");
		out.println(input);
		if(ts.isClosed()) {
			ts = new TransServer();
		}
		
		if(input.equals("shutdown")) {
			ts.toTS("shutdown");
			ts.close();
		}
		else {
			ts.toTS(input);
		}*/
		
		int i = 2;
		String temp;
		String input = request.getParameter("param1");
		while((temp = request.getParameter("param"+i)) != null) {
			input.concat(","+temp);
		}
		
		
		PrintWriter out = response.getWriter();
		//String fromTS = ts.fromTS();
		String fromTS = "HelloWorld";
		out.println("<html>");
		out.println("<body>");
		out.println(fromTS);
		out.println("<form><input type=\"button\" value=\"Back\" onClick=\"history.go(-1); return true;\"></form>");
		out.println("</body>");
		out.println("</html>");
	}

}
