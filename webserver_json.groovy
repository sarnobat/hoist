import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpServer;

public class Server {
	@Path("helloworld")
	public static class HelloWorldResource { // Must be public

		private static final String HIGHER_RANK = "_+1";
		private String _dir = "/Users/sarnobat/Windows/misc/ind/btt";

		//
		// Read-only operations
		//
		@GET
		@Path("json")
		@Produces("application/json")
		public Response json() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("img", _dir + "/wp_Janubaba_DeepikaPadukonebycoolman_201032123814NINCA6.jpg");
			json.put(
					"img2",
					_dir
							+ "/wp_Janubaba_RaimaSen_200542323920O4R141-2335724f84eae731c503f88521bebab6.jpg");
			String[] extensions = { "jpg" };
			Collection<File> files = FileUtils.listFiles(new File(_dir), new IOFileFilter() {

				public boolean accept(File file) {
					return true;
				}

				public boolean accept(File dir, String name) {
					return false;
				}
			}, null);
			System.out.println(files.size());
			for (Object o : files) {
				File f = (File) o;
				json.put(f.getAbsolutePath(), f.getAbsolutePath());
				System.out.println(f.getAbsolutePath());
			}
			System.out.println(json.toString());
			return Response.ok().header("Access-Control-Allow-Origin", "*").entity(json.toString())
					.type("application/json").build();
		}

		//
		// Write operations
		//

		@GET
		@Path("moveUp")
		@Produces("application/json")
		public Response moveUp(@QueryParam("path") String iPath) throws JSONException {
			JSONObject json = new JSONObject();

			append_higher_rank_dir_to_containing_dir: {
				String theContainingDirPath = FilenameUtils.getFullPath(iPath);
				System.out.println();
				String theTargetDirPath = theContainingDirPath + "/" + HIGHER_RANK;
				File theTargetDir = new File(theTargetDirPath);
				boolean createTargetDir = true;
				File fileToMove = new File(iPath);
				if (theTargetDir.exists()) {
					createTargetDir = false;
					String destinationFilePath = theTargetDirPath;
					String extension = FilenameUtils.getExtension(destinationFilePath);
					String destinationFilePathWithoutExtension = destinationFilePath.substring(0,
							destinationFilePath.lastIndexOf('.'));
					File destinationFile = new File(destinationFilePath);
					while (destinationFile.exists()) {
						destinationFilePathWithoutExtension += "1";
						destinationFilePath = destinationFilePathWithoutExtension + "." + extension;
						destinationFile = new File(destinationFilePath);
					}
					// Double check, for now. We don't want to lose any images
					if (new File(destinationFilePath).exists()) {
						throw new RuntimeException("Developer error");
					}
				}
				try {
					FileUtils.moveFileToDirectory(fileToMove, theTargetDir, createTargetDir);
					System.out.println("moveUp() - success");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			System.out.println("moveUp() - " + iPath);
			return Response.ok().header("Access-Control-Allow-Origin", "*").entity(json.toString())
					.type("application/json").build();
		}
	}

	public static void main(String[] args) throws URISyntaxException {
		HttpServer server = JdkHttpServerFactory.createHttpServer(
				new URI("http://localhost:9099/"), new ResourceConfig(HelloWorldResource.class));
	}
}