/**
 * 
 */
package httphandler;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
/**
 * @author loveholly519
 *
 */
public class Lucky extends HttpServlet {

	/**
	 * @param args
	 */
	public void doGet (HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
			PrintWriter out = response.getWriter(); 
			out.println ("Hello World");
			
		}

	/*public static void main(String[] args) {
		// TODO Auto-generated method stub

	}*/

}
