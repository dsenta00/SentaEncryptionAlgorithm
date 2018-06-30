
public class Cryptography {

    private static String key64 = "2afbaecad43128e3";

    public static void main(String [] args) throws Exception {
        System.out.println("0. CREATING HC & IHC using key \"" + key64 + "\"");
        SentaEnc key = new SentaEnc(key64);

        String original = "Ante voli milicu, a milica voli antu";
        String ciphertext = key.encrypt("Ante voli milicu, a milica voli antu");
        String plainText = key.decrypt(ciphertext);

        System.out.println("\toriginal text : \"" + original + "\"");
        System.out.println("\tcyphertext    : \"" + ciphertext + "\"");
        System.out.println("\tdecrypted     : \"" + plainText + "\"");
    }
}
