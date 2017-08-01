package guardias.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.guardias.Util;

public class SecurityUtil {

	
	public static String DesEncriptarTokenEmail(String EncriptedEmail)
	{
		byte[] datosDecifrados=null;	
		SecretKeySpec key = new SecretKeySpec(Util._CLAVE_ENCRIPTACION.getBytes(), "AES");
		Cipher cipher=null;
		
		//EncriptedEmail = EncriptedEmail.replaceAll("\\s+",""); 
		
		byte[] _DecodedEmail =  Base64.getUrlDecoder().decode(EncriptedEmail.getBytes());
		
		try
		{		
		   cipher = Cipher.getInstance("AES");
		   cipher.init(Cipher.DECRYPT_MODE, key);
		   datosDecifrados = cipher.doFinal(_DecodedEmail);
		   //String mensaje_original = new String(datosDecifrados); 
		 //  System.out.println(mensaje_original);
		} 
		catch (Exception e) {
			   e.printStackTrace();
		}
		
		return new String(datosDecifrados);

	}
	
	
	public static String EncriptarTokenEmail(String Email)
	{
		byte[] campoCifrado=null;	
		SecretKeySpec key = new SecretKeySpec(Util._CLAVE_ENCRIPTACION.getBytes(), "AES");
		Cipher cipher=null;
		try
		{
		   cipher = Cipher.getInstance("AES");		 
		   cipher.init(Cipher.ENCRYPT_MODE, key);
		   campoCifrado = cipher.doFinal(Email.getBytes());
		}
		catch (Exception e) {
			   e.printStackTrace();
		}
		//String texto_encriptado = Base64.encodeToString(encrypted, Base64.DEFAULT)
		return Base64.getUrlEncoder().encodeToString(campoCifrado);

	}
	
	public static String GeneratePlainRandomPassword()
	{
		return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6); 


	}
	
	public static String GenerateEncriptedRandomPassword(String PlainPassword)
	{
		
		  String generatedPassword = null;
		  
		  String salt = "";
		  
		  try 
		  {
	         MessageDigest md = MessageDigest.getInstance("SHA-512");
	         md.update(salt.getBytes("UTF-8"));
	         byte[] bytes = md.digest(PlainPassword.getBytes("UTF-8"));
	         /*StringBuilder sb = new StringBuilder();
	         for(int i=0; i< bytes.length ;i++){
	            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	         }
	         generatedPassword = sb.toString();*/
	         
	         generatedPassword = Base64.getEncoder().encodeToString(bytes);
	         
	        } 
	       catch (NoSuchAlgorithmException e){
	        e.printStackTrace();
	       } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 return generatedPassword;

	}
	
	public static void main(String[] args) {
        //El código que iniciará nuestra aplicación
		//System.out.println(EncriptarTokenEmail("refundable.tech@gmail.com"));
		System.out.println(DesEncriptarTokenEmail("bXOznmlG4+1508AcPgBUITMPElVM6KRtkqO7PaXcB/I="));
												//   bXOznmlG4 1508AcPgBUITMPElVM6KRtkqO7PaXcB/I=
    }
	
}
