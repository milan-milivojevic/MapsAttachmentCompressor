package com.imageResize.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AttachmentResponseDTO implements Serializable
{
    private static final long serialVersionUID = 2000576303247723563L;
    
    @JsonProperty("totalElements")
    private Integer totalElements;
    
    @JsonProperty("limit")
    private Integer limit;
    
    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("content")
    private List<AttachmentDTO> attachments;
    
    public AttachmentResponseDTO()
    {
    }

	public Integer getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(Integer totalElements) {
		this.totalElements = totalElements;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}


	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public List<AttachmentDTO> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentDTO> attachments) {
		this.attachments = attachments;
	}
    
}
