package com.brandmaker.cs.skyhigh.imageResize.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AttachmentDTO implements Serializable
{
    private static final long serialVersionUID = 2000576303247723563L;
    
    @JsonProperty("annexAttachmentId")
    private Integer annexAttachmentId;
    
    @JsonProperty("attachmentDate")
    private Date attachmentDate;
    
    @JsonProperty("attachmentFileName")
    private String attachmentFileName;
    
    @JsonProperty("fileSize")
    private Integer fileSize;
    
    @JsonProperty("comment")
    private String comment;
    
    @JsonProperty("source")
    private String source;

	@JsonProperty("externalId")
	private String externalId;

	@JsonProperty("attachmentLink")
	private String attachmentLink;
    
    public AttachmentDTO()
    {
    }

	public Integer getAnnexAttachmentId() {
		return annexAttachmentId;
	}


	public void setAnnexAttachmentId(Integer annexAttachmentId) {
		this.annexAttachmentId = annexAttachmentId;
	}


	public Date getAttachmentDate() {
		return attachmentDate;
	}


	public void setAttachmentDate(Date attachmentDate) {
		this.attachmentDate = attachmentDate;
	}


	public String getAttachmentFileName() {
		return attachmentFileName;
	}


	public void setAttachmentFileName(String attachmentFileName) {
		this.attachmentFileName = attachmentFileName;
	}


	public Integer getFileSize() {
		return fileSize;
	}


	public void setFileSize(Integer fileSize) {
		this.fileSize = fileSize;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getAttachmentLink() {
		return attachmentLink;
	}

	public void setAttachmentLink(String attachmentLink) {
		this.attachmentLink = attachmentLink;
	}
}
