package com.imageResize.service.impl;

import java.awt.image.BufferedImage;
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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imageResize.dto.AttachmentDTO;
import com.imageResize.dto.AttachmentResponseDTO;
import com.imageResize.dto.ListOfChildNodes;
import com.imageResize.dto.NodeDTO;
import com.imageResize.dto.RootNodeDTO;
import com.imageResize.dto.TreeDTO;
import com.imageResize.service.AuthorizationService;
import com.imageResize.service.ImageResize;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ImageResizeImpl implements ImageResize{
	
	private ObjectMapper objectMapper = new ObjectMapper();
	private static final String FILE_PREFIX="REPORT_REDUCED_";
	public int totalNodes;
	public int processedNodes;
	private String serverMainUrl;
	
	 @Autowired
	 private Environment env;
	
	@Override
	public int getTotalNodes() {
		return totalNodes;
	}
	
	@PostConstruct
	private void init() {
		serverMainUrl = env.getProperty("server.main.path");
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

	@Autowired
	AuthorizationService authorizationService;

	@Override
	public void resizeScheduled() {
	}

	@Override
	public void resizeTest() throws Exception {
	        //List<NodeDTO> level9 = getLevelNineNodes();
	    	//List<NodeDTO> testNodeList = new ArrayList<NodeDTO>();
	    	//NodeDTO testN = new NodeDTO();
	    	//testN.setId(255965);
	    	//testNodeList.add(testN);
	        //downloadAttachment(45704, 255965);
			//totalNodes = level9.size();
	        //process(level9);
			log.info(serverMainUrl);
	}
	
    public static void main(String[] args) throws Exception {

        //List<NodeDTO> level9 = getLevelNineNodes();
    	//List<NodeDTO> testNodeList = new ArrayList<NodeDTO>();
    	//NodeDTO testN = new NodeDTO();
    	//testN.setId(255965);
    	//testNodeList.add(testN);
        //downloadAttachment(45704, 255965);

        //process(testNodeList);
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
        if(!level9.isEmpty()) {
            return level9;
        }else {
            throw new Exception("Can not reach level nine nodes!");
        }

    }

	private void process(List<NodeDTO> levelNine) throws IOException {

		for (NodeDTO n : levelNine) {

			AttachmentResponseDTO allAttachments = getAttachmentsForNode(n.getId());
			for (AttachmentDTO attach : allAttachments.getAttachments()) {
				processedNodes++;
				if (isFIleExtensionAccepted(attach)
						&& !checkIfAttachmentIsProccessed(attach.getAttachmentFileName(), allAttachments)) {
					// call resize and upload
					File resized = resizeAttachment(attach, n.getId());
					uploadAttachment(n.getId(), resized, attach.getComment());
				}
			}
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

    private static boolean checkIfAttachmentIsProccessed(String filename, AttachmentResponseDTO allAttachments) {

        for(AttachmentDTO attach : allAttachments.getAttachments()) {
            if(attach.getAttachmentFileName().contains(FILE_PREFIX)) {
                return true;
            }
        }

        return false;
    }

    private File resizeAttachment(AttachmentDTO attach, Integer nodeId) throws IOException {
        File outputfile = downloadAttachment(attach.getAnnexAttachmentId(), nodeId, attach.getAttachmentFileName());
        
        InputStream targetStream = new FileInputStream(outputfile);

        BufferedImage image = ImageIO.read(targetStream);
        int originalHeight = image.getHeight();
        int originalWidth = image.getWidth();

        int differnece =originalHeight-500;
        int resizedWidth = originalWidth-differnece;

        String fileExtension = FilenameUtils.getExtension(attach.getAttachmentFileName());
        
        BufferedImage dimg = null;
        if(originalHeight > 500) {
        	dimg = resize(FILE_PREFIX + outputfile.getName(), image, 500, resizedWidth);	
        	
        	File resizedFile = new File(FILE_PREFIX + outputfile.getName());
            ImageIO.write(dimg, fileExtension, resizedFile);
            return resizedFile;
        }
        else {
        	File resizedFile = new File(FILE_PREFIX + outputfile.getName());
            ImageIO.write(image, fileExtension, resizedFile);
        	return resizedFile;
        }

    }


    private File downloadAttachment(Integer attachmentId, Integer nodeId, String filename) {

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
        	
            String url =serverMainUrl+ "/attachment/download/" + attachmentId + "/";

            HttpGet getRequest = new HttpGet(url);

            getRequest.addHeader("Authorization", "Bearer " + authorizationService.getToken().replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

            HttpResponse response = httpClient.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Failed with HTTP error code : " + statusCode);
            }
            
            File downloadedFile = new File(filename);
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

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            String url = serverMainUrl+ "/attachment/node/" + nodeId + "/";

            HttpPost postReq = new HttpPost(url);
            postReq.addHeader("Authorization", "Bearer " + authorizationService.getToken().replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));
            
            MultipartEntityBuilder entity = MultipartEntityBuilder.create()
                    .setMimeSubtype("mixed")
                    .addTextBody("attachment", "{ \"name\": \""+outputfile.getName()+"\", \"comment\": \""+comment+"\", \"link\": \"http://miro.com\", \"newWindow\": false }", ContentType.APPLICATION_JSON)
                    .addPart(FormBodyPartBuilder.create()
                            .setName("file")
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

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            String url = serverMainUrl+ "/attachment/node/" + nodeId + "/";

            HttpGet getRequest = new HttpGet(url);

            getRequest.addHeader("Authorization", "Bearer "
                    + authorizationService.getToken().replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

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
            String url = serverMainUrl+ "/node/child/replaceRootNode/from-tree/replaceFromTree/to-tree/replaceToTree?withAdditionalInfo=false";

            HttpGet getRequest = new HttpGet(url.replace("replaceRootNode", "" + rootNodeId)
                    .replace("replaceFromTree", "" + fromTreeId).replace("replaceToTree", "" + toTreeId));

            getRequest.addHeader("Authorization", "Bearer "
                    + authorizationService.getToken().replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

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
            HttpGet getRequest = new HttpGet(serverMainUrl+ "/node/root/24");

            getRequest.addHeader("Authorization", "Bearer "
                    + authorizationService.getToken().replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

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
            HttpGet getRequest = new HttpGet(serverMainUrl+ "/tree");

            getRequest.addHeader("Authorization", "Bearer "
                    + authorizationService.getToken().replace("{\"access_token\":", "").replace("}", "").replace("\"", ""));

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

    private RootNodeDTO convertNode(String apiOutput)
            throws JsonParseException, JsonMappingException, IOException {

        return (RootNodeDTO) objectMapper.readValue(apiOutput, RootNodeDTO.class);
    }

    private List<TreeDTO> convertTreeFromEntity(String apiOutput)
            throws ParseException, IOException {

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

    synchronized public static BufferedImage resize(String filename, BufferedImage orig, int width, int height)
            throws IOException {
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
        return null;
    }


}
