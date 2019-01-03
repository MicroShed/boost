package io.openliberty.boost.common.config;

public class BoosterDependencyInfo {

	protected String group;
	protected String artifact;
	protected String version;
	
	public BoosterDependencyInfo(String grp, String artf, String ver ) {
		
		this.group = grp;
		this.artifact = artf;
		this.version = ver;
	}
	
	public String getGroup(){
		return group;
	}
	
	public String getArtifact(){
		return artifact;
	}
	
	public String getVersion(){
		return version;
	}
}
