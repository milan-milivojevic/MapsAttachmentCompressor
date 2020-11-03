package com.brandmaker.cs.skyhigh.imageResize.service.impl;

import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import com.brandmaker.cs.skyhigh.imageResize.dto.AttachmentDTO;
import com.brandmaker.cs.skyhigh.imageResize.dto.AttachmentResponseDTO;
import com.brandmaker.cs.skyhigh.imageResize.dto.ListOfChildNodes;
import com.brandmaker.cs.skyhigh.imageResize.dto.NodeDTO;
import com.brandmaker.cs.skyhigh.imageResize.dto.RootNodeDTO;
import com.brandmaker.cs.skyhigh.imageResize.dto.TreeDTO;
import com.brandmaker.cs.skyhigh.imageResize.service.AuthorizationService;
import com.brandmaker.cs.skyhigh.imageResize.service.ExcelConverter;
import com.brandmaker.cs.skyhigh.imageResize.service.ImageResize;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@EnableAsync
@Slf4j
public class ImageResizeImpl implements ImageResize {
	
	private static final int HEIGHT=1024;

	@Autowired
	private Environment env;
	
	@Autowired
	AuthorizationService authorizationService;

	@Autowired
	ExcelConverter excelConverter;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	private static final String FILE_PREFIX = "REPORT_REDUCED_";
	public int totalNodes;
	public int processedNodes;
	private String serverMainUrl;
	private String fileTmpPath;

	@Override
	public int getTotalNodes() {
		return totalNodes;
	}

	@PostConstruct
	private void init() {
		serverMainUrl = env.getProperty("server.main.path");
		fileTmpPath = env.getProperty("file.tmp.path");
	}

	public void setTotalNodes(int totalNodes) {
		this.totalNodes = totalNodes;
	}

	@Override
	public int getProcessedNodes() {
		return processedNodes;
	}

	public void setProcessedNodes(int processedNodes) {
		this.processedNodes = processedNodes;
	}

	@Override
	public void resizeScheduled() {
	}

	private void cleanTmpFolder() {
		try {
			FileUtils.cleanDirectory(new File(fileTmpPath));
		} catch (IOException e) {
			log.error("Can not delete files in tmp folder:"+fileTmpPath,e);
		}
	}

	private List<NodeDTO> getLevelNineNodes() throws Exception {
		List<TreeDTO> tree = getMainTree();
		int beginTreeId = 0;
		int endTreeId = 0;

		if (tree != null) {
			beginTreeId = tree.get(0).getId();
		}
		if (tree != null && tree.size() > 1) {
			endTreeId = tree.get(tree.size() - 1).getId();
		}

		RootNodeDTO rootNode = getRootNode();
		ListOfChildNodes childNodesFirstLevel = getChildNodes(beginTreeId, endTreeId, rootNode.getId());
		List<NodeDTO> level1 = childNodesFirstLevel.getAllDTO();
		List<NodeDTO> level2 = new ArrayList<NodeDTO>();
		List<NodeDTO> level3 = new ArrayList<NodeDTO>();
		List<NodeDTO> level4 = new ArrayList<NodeDTO>();
		List<NodeDTO> level5 = new ArrayList<NodeDTO>();
		List<NodeDTO> level6 = new ArrayList<NodeDTO>();
		List<NodeDTO> level7 = new ArrayList<NodeDTO>();
		List<NodeDTO> level8 = new ArrayList<NodeDTO>();
		List<NodeDTO> level9 = new ArrayList<NodeDTO>();

		for (NodeDTO n : level1) {
			log.info("Level 1 Searching for: " + n.getId());
			level2.addAll(getChildNodes(beginTreeId, endTreeId, n.getId()).getAllDTO());
		}

		for (NodeDTO n : level2) {
			log.info("Level 2 Searching for: " + n.getId());
			level3.addAll(getChildNodes(beginTreeId, endTreeId, n.getId()).getAllDTO());
		}
		for (NodeDTO n : level3) {
			log.info("Level 3 Searching for: " + n.getId());
			level4.addAll(getChildNodes(beginTreeId, endTreeId, n.getId()).getAllDTO());
		}
		for (NodeDTO n : level4) {
			log.info("Level 4 Searching for: " + n.getId());
			level5.addAll(getChildNodes(beginTreeId, endTreeId, n.getId()).getAllDTO());
		}
		for (NodeDTO n : level5) {
			log.info("Level 5 Searching for: " + n.getId());
			level6.addAll(getChildNodes(beginTreeId, endTreeId, n.getId()).getAllDTO());
		}
		for (NodeDTO n : level6) {
			log.info("Level 6 Searching for: " + n.getId());
			level7.addAll(getChildNodes(beginTreeId, endTreeId, n.getId()).getAllDTO());
		}
		for (NodeDTO n : level7) {
			log.info("Level 7 Searching for: " + n.getId());
			level8.addAll(getChildNodes(beginTreeId, endTreeId, n.getId()).getAllDTO());
		}
		for (NodeDTO n : level8) {
			log.info("Level 8 Searching for: " + n.getId());
			level9.addAll(getChildNodes(beginTreeId, endTreeId, n.getId()).getAllDTO());
		}
		for (NodeDTO n : level9) {
			log.info("Level 9 id: " + n.getId() + " name:" + n.getName());
		}
		if (!level9.isEmpty()) {
			return level9;
		} else {
			throw new Exception("Can not reach level nine nodes!");
		}

	}

	private void process(List<Integer> levelNine) throws IOException {

		for (Integer n : levelNine) {
			log.info("processing node id: " + n);
			AttachmentResponseDTO allAttachments = getAttachmentsForNode(n);

			if (allAttachments != null) {
				log.info("received attachemnts for node id: " + n + " count=" + allAttachments.getTotalElements());

				for (AttachmentDTO attach : allAttachments.getAttachments()) {
					processedNodes++;
					if (isFIleExtensionAccepted(attach)
							&& !isAttachmentProcessed(attach.getAttachmentFileName(), allAttachments)) {
						log.info("processing RESIZE for node id: " + n + " for attachment name:"
								+ attach.getAttachmentFileName());
						File resized = resizeAttachment(attach, n);
						
						if (resized != null) {
							uploadAttachment(n, resized, attach.getComment());
						} else {
							log.info("SOMETHING WENT WRONG FOR NODE ID:" + n + " ATTACHMENT ID:"
									+ attach.getAnnexAttachmentId());
						}
					}
				}
			}
		}
		emptyTmpFolder();
		log.info("Proccess is DONE!!!");
	}
	
	private void emptyTmpFolder() {
		try {
			File directory = new File(fileTmpPath);
			FileUtils.cleanDirectory(directory);
		} catch (Exception e) {
		}
	}

	private boolean isFIleExtensionAccepted(AttachmentDTO attach) {
		String fileExtension = FilenameUtils.getExtension(attach.getAttachmentFileName());
		if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")
				|| fileExtension.equalsIgnoreCase("png")) {
			return true;
		}
		return false;
	}

	private static boolean isAttachmentProcessed(String filename, AttachmentResponseDTO allAttachments) {

		for (AttachmentDTO attach : allAttachments.getAttachments()) {
			if(filename.contains(FILE_PREFIX)) {
				return true;
			}
			else if (attach.getAttachmentFileName().equalsIgnoreCase(FILE_PREFIX+filename)) {
				return true;
			}
		}
		return false;
	}
	
	private File resizeAttachment(AttachmentDTO attach, Integer nodeId) throws IOException {
		File outputfile = downloadAttachment(attach.getAnnexAttachmentId(), nodeId, attach.getAttachmentFileName());
		try {
			if (outputfile != null) {
				InputStream targetStream = new FileInputStream(outputfile);

				BufferedImage image = ImageIO.read(targetStream);
				int originalHeight = image.getHeight();
				int originalWidth = image.getWidth();
				int differnece = 0;
				int resizedWidth = 0;
				int resizeHeight = 0;

				if (originalHeight > HEIGHT) {
					if (originalHeight <= originalWidth) {
						differnece = originalHeight - HEIGHT;
						resizedWidth = originalWidth - differnece;
						resizeHeight = HEIGHT;

					} else {
						resizeHeight = HEIGHT;
						resizedWidth = originalWidth;
						// h = 600
						// w 100
					}
				} else {
					resizeHeight = originalHeight;
					resizedWidth = originalWidth;
				}

				log.info("resizeHeight = " + resizeHeight);
				log.info("resizedWidth = " + resizedWidth);

				String fileExtension = FilenameUtils.getExtension(attach.getAttachmentFileName());

				BufferedImage dimg = null;
				if (originalHeight > HEIGHT) {
					dimg = resize(FILE_PREFIX + outputfile.getName(), image, resizeHeight, resizedWidth);

					File resizedFile = new File(fileTmpPath,FILE_PREFIX + outputfile.getName());
					ImageIO.write(dimg, fileExtension, resizedFile);
					return resizedFile;
				} else {
					File resizedFile = new File(fileTmpPath,FILE_PREFIX + outputfile.getName());
					ImageIO.write(image, fileExtension, resizedFile);
					return resizedFile;
				}
			}
		} catch (Exception e) {

		}
		return null;
	}

	private InputStream downloadAttachmentINS(Integer attachmentId, Integer nodeId, String filename) {

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			String url = serverMainUrl + "/attachment/download/" + attachmentId + "/";

			HttpGet getRequest = new HttpGet(url);

			getRequest.addHeader("Authorization", "Bearer " + authorizationService.getToken()
					.replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

			HttpResponse response = httpClient.execute(getRequest);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new RuntimeException("Failed to download attachment with id:"+attachmentId+" for NODE ID:"+nodeId+" with HTTP error code : " + statusCode);
			}
			InputStream in=response.getEntity().getContent();
			return in;

		} catch (Exception e) {
			log.error(e.toString());
		}
		return null;

	}
	
	private File downloadAttachment(Integer attachmentId, Integer nodeId, String filename) {

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			String url = serverMainUrl + "/attachment/download/" + attachmentId + "/";

			HttpGet getRequest = new HttpGet(url);

			getRequest.addHeader("Authorization", "Bearer " + authorizationService.getToken()
					.replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

			HttpResponse response = httpClient.execute(getRequest);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new RuntimeException("Failed to download attachment with id:"+attachmentId+" for NODE ID:"+nodeId+" with HTTP error code : " + statusCode);
			}

			File downloadedFile = new File(fileTmpPath,filename);
			try {
				FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
			} finally {
				response.getEntity().getContent().close();
			}
			return downloadedFile;

		} catch (Exception e) {
			log.error(e.toString());
		}
		return null;

	}

	private void uploadAttachment(Integer nodeId, File outputfile, String comment) {
		log.info("UPLOADING attachment for node id:" + nodeId + "  file NAME: "+outputfile.getName());
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			String url = serverMainUrl + "/attachment/node/" + nodeId + "/";

			HttpPost postReq = new HttpPost(url);
			postReq.addHeader("Authorization", "Bearer " + authorizationService.getToken()
					.replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

			MultipartEntityBuilder entity = MultipartEntityBuilder.create().setMimeSubtype("mixed")
					.addTextBody("attachment",
							"{ \"name\": \"" + outputfile.getName() + "\", \"comment\": \"" + comment
									+ "\", \"link\": \"\", \"newWindow\": false }",
							ContentType.APPLICATION_JSON)
					.addPart(FormBodyPartBuilder.create().setName("file")
							.setBody(new ByteArrayBody(Files.readAllBytes(outputfile.toPath()), outputfile.getName()))
							.build());

			postReq.setEntity(entity.build());

			HttpResponse response = httpClient.execute(postReq);

			HttpEntity httpEntity = response.getEntity();
			String apiOutput = EntityUtils.toString(httpEntity);
			log.debug(apiOutput);

		} catch (Exception e) {
			e.toString();
		}

	}

	private AttachmentResponseDTO getAttachmentsForNode(Integer nodeId) {
		log.info("getting attachments for node :" + nodeId);
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			String url = serverMainUrl + "/attachment/node/" + nodeId + "/";

			HttpGet getRequest = new HttpGet(url);

			getRequest.addHeader("Authorization", "Bearer " + authorizationService.getToken()
					.replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

			HttpResponse response = httpClient.execute(getRequest);

			// verify the valid error code first
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new RuntimeException("Failed with HTTP error code : " + statusCode);
			}

			// Now pull back the response object
			HttpEntity httpEntity = response.getEntity();
			String apiOutput = EntityUtils.toString(httpEntity);

			// Lets see what we got from API
			System.out.println(apiOutput); // <user id="10"><firstName>demo</firstName><lastName>user</lastName></user>

			ObjectMapper objectMapper = new ObjectMapper();

			AttachmentResponseDTO attachmentsResponse = (AttachmentResponseDTO) objectMapper.readValue(apiOutput,
					AttachmentResponseDTO.class);
			return attachmentsResponse;
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return null;

	}

	private ListOfChildNodes getChildNodes(Integer fromTreeId, Integer toTreeId, Integer rootNodeId) {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			String url = serverMainUrl
					+ "/node/child/replaceRootNode/from-tree/replaceFromTree/to-tree/replaceToTree?withAdditionalInfo=false";

			HttpGet getRequest = new HttpGet(url.replace("replaceRootNode", "" + rootNodeId)
					.replace("replaceFromTree", "" + fromTreeId).replace("replaceToTree", "" + toTreeId));

			getRequest.addHeader("Authorization", "Bearer " + authorizationService.getToken()
					.replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

			HttpResponse response = httpClient.execute(getRequest);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new RuntimeException("Failed with HTTP error code : " + statusCode);
			}

			HttpEntity httpEntity = response.getEntity();
			String apiOutput = EntityUtils.toString(httpEntity);

			log.debug(apiOutput);

			ObjectMapper objectMapper = new ObjectMapper();

			ListOfChildNodes nodes = (ListOfChildNodes) objectMapper.readValue(apiOutput, ListOfChildNodes.class);
			return nodes;
		} catch (Exception e) {
			log.error(e.toString());
		}
		return null;
	}

	private RootNodeDTO getRootNode() {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpGet getRequest = new HttpGet(serverMainUrl + "/node/root/24");

			getRequest.addHeader("Authorization", "Bearer " + authorizationService.getToken()
					.replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

			HttpResponse response = httpClient.execute(getRequest);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new RuntimeException("Failed with HTTP error code : " + statusCode);
			}

			HttpEntity httpEntity = response.getEntity();
			String apiOutput = EntityUtils.toString(httpEntity);

			System.out.println(apiOutput);

			ObjectMapper objectMapper = new ObjectMapper();

			RootNodeDTO node = (RootNodeDTO) objectMapper.readValue(apiOutput, RootNodeDTO.class);
			return node;
		} catch (Exception e) {
			log.error(e.toString());
		}
		return null;
	}

	private List<TreeDTO> getMainTree() throws Exception {

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpGet getRequest = new HttpGet(serverMainUrl + "/tree");

			getRequest.addHeader("Authorization", "Bearer " + authorizationService.getToken()
					.replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

			HttpResponse response = httpClient.execute(getRequest);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != 200) {
				throw new RuntimeException("Failed with HTTP error code : " + statusCode);
			}

			HttpEntity httpEntity = response.getEntity();
			String apiOutput = EntityUtils.toString(httpEntity);
			System.out.println(apiOutput);
			List<TreeDTO> tree = convertTreeFromEntity(apiOutput);
			List<TreeDTO> finalTree = new ArrayList<TreeDTO>();
			for (TreeDTO t : tree) {
				if (Integer.parseInt(t.getStartDate().substring(0, 4)) >= 2019) {
					finalTree.add(t);
				}
			}

			return finalTree;
		} catch (IOException e) {
			log.error(e.toString());
			return null;
		}
	}

	private RootNodeDTO convertNode(String apiOutput) throws JsonParseException, JsonMappingException, IOException {

		return (RootNodeDTO) objectMapper.readValue(apiOutput, RootNodeDTO.class);
	}

	private List<TreeDTO> convertTreeFromEntity(String apiOutput) throws ParseException, IOException {

		TreeDTO[] treeList = (TreeDTO[]) objectMapper.readValue(apiOutput, TreeDTO[].class);
		return Arrays.asList(treeList);
	}

	synchronized private static String getFormatName(String filename) throws IOException {
		String ext = filename.substring(filename.lastIndexOf(".") + 1);
		String[] arr = ImageIO.getWriterFormatNames();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(ext))
				return ext;
		}
		throw new IOException("file [" + filename + "] has a format not supported [" + ext + "]");
	}

	synchronized public static BufferedImage resize(String filename, BufferedImage orig, int width, int height) {
		try {
			if (width > 0 || height > 0) {

				Mode mode = Mode.AUTOMATIC;
				if (height == 0)
					mode = Mode.FIT_TO_WIDTH;
				else if (width == 0)
					mode = Mode.FIT_TO_HEIGHT;

				BufferedImage src = orig;
				BufferedImage thumbnail = null;
				if (src.getHeight() < height && src.getWidth() < width) {
					thumbnail = src;
				} else {
					thumbnail = Scalr.resize(src, Method.ULTRA_QUALITY, mode, width, height);
				}
				return thumbnail;
			}
		} catch (IllegalArgumentException e) {
			log.error("IllegalArgumentException height:" + height + " width:" + width, e);
		} catch (ImagingOpException ex) {
			log.error("ImagingOpException: ", ex);
		}
		return null;
	}

	@Override
	public void runResize() {
        List<Integer> levelNine = excelConverter.readFile();
        //List<Integer> levelNine = new ArrayList<Integer>(); levelNine.add(163794);
        try {
			process(levelNine);
		} catch (IOException e) {
			log.error("Error in resize processing:",e);
		}
	}

}
