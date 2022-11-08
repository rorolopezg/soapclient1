package cl.rmlg.soap.client1;

import cl.rmlg.soap.client1.generated.FndPersonByPIDRqType;
import cl.rmlg.soap.client1.generated.FndPersonByPIDRsType;
import cl.rmlg.soap.client1.generated.InsisPDM;
import cl.rmlg.soap.client1.generated.InsisPDMPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@SpringBootApplication
public class Soapclient1Application {

	public static void main(String[] args) {
		SpringApplication.run(Soapclient1Application.class, args);
	}


	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dumpTreshold", "999999");

			URL url = new URL("http://10.1.4.201:9179/insisws/InsisPAMPort?wsdl");
			System.out.println("Iniciando...");
			WebServiceFeature wsf;
			InsisPDM pamService = new InsisPDM();
			pamService.setHandlerResolver(new HandlerResolver() {
				public List<Handler> getHandlerChain(PortInfo portInfo) {
					System.out.println("++++++++++++++++++");
					List<Handler> handlerList = new ArrayList<Handler>();
					handlerList.add(new RGBSOAPHandler());
					return handlerList;
				}
			});
			InsisPDMPort insisPDMPort = pamService.getInsisPDMPort();


			//((BindingProvider)insisPDMPort).getRequestContext().put("ws-security.username", "insis_gen_v10");
			//((BindingProvider)insisPDMPort).getRequestContext().put("ws-security.password", "Uy-Dev2022");

			System.out.println("Generando manejador para seguridad...");
			//SecurityPoliciesFeature securityFeatures =
			//    new SecurityPoliciesFeature(new String[] { "oracle/wss_username_token_client_policy" });
			//Map<String, Object> reqContext =    ((BindingProvider)insisPAMPort).getRequestContext();

			//reqContext.put(BindingProvider.USERNAME_PROPERTY, "insis_gen_v10");
			//reqContext.put(BindingProvider.PASSWORD_PROPERTY, "Uy-Dev2022");

			//binding.setHandlerChain(handlerChain);

			System.out.println("Llamando al servicio...");

			FndPersonByPIDRqType payload = new FndPersonByPIDRqType();
			payload.setPID("RUT6000001283");
			FndPersonByPIDRsType policyByNumber =insisPDMPort.findPersonByPID(payload);
			System.out.println(policyByNumber.getEntity().getPersonalData().getName());
		};
	}

	class RGBSOAPHandler implements SOAPHandler<SOAPMessageContext> {

		public Set<QName> getHeaders() {
			return new TreeSet();
		}

		public boolean handleMessage(SOAPMessageContext context) {
			System.out.println("*************");
			Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			if (outboundProperty.booleanValue()) {
				System.out.println("************* Seteando cabaceras");
				SOAPMessage message = context.getMessage();
				try {
					SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
					if (envelope.getHeader() != null) {
						envelope.getHeader().detachNode();
					}
					//SOAPHeader header = envelope.addHeader();
					SOAPFactory factory = SOAPFactory.newInstance();
					String prefix = "wsse";
					String uri = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
					SOAPElement securityElem = factory.createElement("Security",prefix,uri);
					SOAPElement usernameToken = factory.createElement("UsernameToken");
					SOAPElement username = factory.createElement("Username");
					username.addTextNode("insis_gen_v10");
					SOAPElement password = factory.createElement("Password");
					password.addTextNode("Uy-Dev2022");

					usernameToken.addChildElement(username);
					usernameToken.addChildElement(password);
					securityElem.addChildElement(usernameToken);
					SOAPHeader header = envelope.addHeader();
					header.addChildElement(securityElem);

				} catch (Exception e) {
					System.out.println("Exception in handler: " + e);
				}
			} else {
				// inbound
			}
			return true;
		}

		public boolean handleFault(SOAPMessageContext context) {
			throw new UnsupportedOperationException("Error:"
					+ " -> " + context.getMessage().getContentDescription());
		}

		public void close(MessageContext context) {
			//
		}
	}
}
