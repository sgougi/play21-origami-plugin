import play.Application;
import play.GlobalSettings;

//import com.orientechnologies.orient.core.id.ORID;
//import com.wingnest.play2.origami.GraphDB;
//import com.wingnest.play2.origami.IdManager.IdHandler;

public class Global extends GlobalSettings {

	public void onStart(Application app) {
/*	default	
		GraphDB.setIdHandler(new IdHandler() {

			@Override
			public String encode(ORID arg0) {
				return play.libs.Crypto.encryptAES(arg0.toString());
			}

			@Override
			public String decode(String arg0) {
				return play.libs.Crypto.decryptAES(arg0);
			}

		});
*/		 
	}

}
