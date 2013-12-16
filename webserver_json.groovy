import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
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
		private static final String LOWER_RANK = "_-1";
		
		//
		// Read-only operations
		//
		@GET
		@Path("json")
		@Produces("application/json")
		public Response json(@QueryParam("dir") String iPath) throws JSONException {
			
			String[] extensions = { "jpg" };
			String dir = URLDecoder.decode(iPath);
			Collection<File> files = FileUtils.listFiles(new File(dir), new IOFileFilter() {

				public boolean accept(File file) {
					return true;
				}

				public boolean accept(File dir2, String name) {
					return false;
				}
			}, null);
			System.out.println(files.size());
			JSONObject outerJson = new JSONObject();
			// Level zero
			JSONObject json = new JSONObject();
			for (Object o : files) {
				File f = (File) o;
				json.put(f.getAbsolutePath(), f.getAbsolutePath());
				System.out.println(f.getAbsolutePath());
			}
			outerJson.put("0",json);
			System.out.println(json.toString());
			return Response.ok().header("Access-Control-Allow-Origin", "*").entity(outerJson.toString())
					.type("application/json").build();
		}

		//
		// Write operations
		//

		@GET
		@Path("moveUp")
		@Produces("application/json")
		public Response moveUp(@QueryParam("path") String iPath) throws JSONException {
			System.out.println("moveUp() - " + iPath);

			String targetSubdirName = HIGHER_RANK;

			JSONObject json = new JSONObject();

			moveFileToSubfolder(iPath, targetSubdirName);

			System.out.println("moveUp() - end");
			return Response.ok().header("Access-Control-Allow-Origin", "*").entity(json.toString())
					.type("application/json").build();
		}
		@GET
		@Path("moveDown")
		@Produces("application/json")
		public Response moveDown(@QueryParam("path") String iPath) throws JSONException {
			System.out.println("moveUp() - " + iPath);

			String targetSubdirName = LOWER_RANK;

			JSONObject json = new JSONObject();

			moveFileToSubfolder(iPath, targetSubdirName);

			System.out.println("moveUp() - end");
			return Response.ok().header("Access-Control-Allow-Origin", "*").entity(json.toString())
					.type("application/json").build();
		}

		private static void moveFileToSubfolder(String iPath, String targetSubdirName) {
			_1: {
				String theContainingDirPath = FilenameUtils.getFullPath(iPath);
				System.out.println();
				String theTargetDirPath = theContainingDirPath + "/" + targetSubdirName;
				File theTargetDir = new File(theTargetDirPath);
				boolean createTargetDir = true;
				File fileToMove = new File(iPath);
				String destinationFilePath;
				String fileShortName = FilenameUtils.getName(iPath);
				System.out.println("moveUp() - checking if exists");
				if (theTargetDir.exists()) {
					System.out
							.println("moveUp() - dir already exists, need to make sure existing file is not overwritten");
					createTargetDir = false;
					destinationFilePath = theTargetDirPath + "/" + fileShortName;
					String extension = FilenameUtils.getExtension(destinationFilePath);
					String destinationFilePathWithoutExtension = destinationFilePath.substring(0,
							destinationFilePath.lastIndexOf('.'));
					File destinationFile = new File(destinationFilePath);
					while (destinationFile.exists()) {
						destinationFilePathWithoutExtension += "1";
						destinationFilePath = destinationFilePathWithoutExtension + "." + extension;
						destinationFile = new File(destinationFilePath);
					}
					fileShortName = FilenameUtils.getName(destinationFilePath);
					// Double check, for now. We don't want to lose any images
					if (destinationFile.exists()) {
						throw new RuntimeException("Developer error");
					}
				}

				try {
					System.out.println("moveUp() - about to move");
					FileUtils.moveFileToDirectory(fileToMove, theTargetDir, createTargetDir);
					System.out.println("moveUp() - success");
					check: {
						File newFile = new File(theTargetDirPath + "/" + fileShortName);
						// check that it exists
						if (!newFile.exists()) {
							throw new RuntimeException("Developer error");
						}
						System.out.println("moveUp() - " + newFile.getAbsolutePath());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws URISyntaxException {
		HttpServer server = JdkHttpServerFactory.createHttpServer(
				new URI("http://localhost:9099/"), new ResourceConfig(HelloWorldResource.class));
	}
}