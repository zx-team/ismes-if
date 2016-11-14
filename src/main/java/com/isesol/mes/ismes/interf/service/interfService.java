package com.isesol.mes.ismes.interf.service;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;

public class interfService {
	
	/**查询设备状态汇总信息
	 * @param parameters
	 * @param bundle
	 * @throws Exception
	 */
	public void ifService_tjxx(Parameters parameters, Bundle bundle) throws Exception {
		String sbbh = parameters.getString("sbbh");
		String sbztdm = parameters.getString("sbztdm");
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(sbbh)) 
		{
			con = con + " and sbbh = ? ";
			val.add(sbbh);
		}
		if(StringUtils.isNotBlank(sbztdm))
		{
			con = con + " and sbztdm = ?";
			val.add(sbztdm);
		}
		
		int page = Integer.parseInt(parameters.get("page").toString());
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString());
		//Dataset dataset_tjxx= Sys.query("interf_sbzthzb"？,"sbzthzid,sbbh,sbztdm,ztkssj,ztjssj,xtdqsj", con , null, (page-1)*pageSize, pageSize, val.toArray());
		Dataset dataset_tjxx= Sys.query("interf_sbztjk","sbztjkid,sbbh,sbztdm,sjkssj,sjjssj,xtdqsj", con , null, (page-1)*pageSize, pageSize, val.toArray());
		bundle.put("tjxx", dataset_tjxx.getList());
		int totalPage = dataset_tjxx.getTotal()%pageSize==0?dataset_tjxx.getTotal()/pageSize:dataset_tjxx.getTotal()/pageSize+1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset_tjxx.getTotal());
	}
	
	/**累计时间
	 * @param parameters
	 * @param bundle
	 * @throws Exception
	 */
	public void ifService_ljsj(Parameters parameters, Bundle bundle) throws Exception {
		String sbbh = parameters.getString("sbbh");
		
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(sbbh)) 
		{
			con = con + " and sbbh = ? ";
			val.add(sbbh);
		}
		if(null!=parameters.get("date_start")&&null!=parameters.get("date_end")) 
		{
			Date date_start = parameters.getDate("date_start");
			Date date_end = parameters.getDate("date_end");
			con = con + " and ((sjkssj < ? and sjkssj > ?) or (sjjssj < ? and sjjssj > ?)) ";
			val.add(date_end);
			val.add(date_start);
			val.add(date_end);
			val.add(date_start);
			
		}
		//Dataset dataset_ljsj= Sys.query("interf_sbzthzb"？,"sbzthzid,sbbh,sbztdm,ztkssj,ztjssj,xtdqsj", con , null,  val.toArray());
		Dataset dataset_ljsj= Sys.query("interf_sbztjk","sbztjkid,sbbh,sbztdm,sjkssj,sjjssj,xtdqsj", con , "sjkssj",  val.toArray());
		bundle.put("ljsj", dataset_ljsj.getList());
	}
	
	/**查询故障信息
	 * @param parameters
	 * @param bundle
	 * @throws Exception
	 */
	public void ifService_gzxx(Parameters parameters, Bundle bundle) throws Exception {
		String sbbh = parameters.getString("sbbh");
		Date querytime = parameters.getDate("querytime");
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(sbbh)) 
		{
			con = con + " and sbbh = ? ";
			val.add(sbbh);
		}
			con = con + " and sjsj = ? ";
			val.add(querytime);
		Dataset dataset_gzxx= Sys.query("interf_bj","bjid,sbbh,sjsj,xtdqsj,xzgzzt,zzgzzt", con , null,  val.toArray());
		bundle.put("gzxx", dataset_gzxx.getList());
	}
	
	public void ifService_ssxx(Parameters parameters, Bundle bundle) throws Exception {
		String sbbh = parameters.getString("sbbh");
		Date querytime = parameters.getDate("querytime");
		List<Object> val = new ArrayList<Object>();
		val.add(sbbh);
		val.add(querytime);
		Map<String, Object> mapsbjk = new HashMap<String, Object>();
		//Dataset dataset_sbzt= Sys.query("interf_sbzthzb"？,"max(ztkssj)", "sbbh = ? and ztkssj < ?" , " sbbh ",null,  val.toArray());
		Dataset dataset_sbzt= Sys.query("interf_sbztjk","max(sjkssj)", "sbbh = ? and sjkssj < ?" , " sbbh ",null,  val.toArray());
		if(dataset_sbzt.getList().size()>0)
		{
			//Dataset dataset_zt= Sys.query("interf_sbzthzb"？,"sbzthzid,sbbh,sbztdm,ztkssj,ztjssj,xtdqsj", "sbbh = ? and ztkssj = ?" , null,  new Object[]{sbbh , dataset_sbzt.getList().get(0).get("ztkssj")});
			Dataset dataset_zt= Sys.query("interf_sbztjk","sbztjkid,sbbh,sbztdm,sjkssj,sjjssj,xtdqsj", "sbbh = ? and sjkssj = ?" , null,  new Object[]{sbbh , dataset_sbzt.getList().get(0).get("sjkssj")});
			mapsbjk = dataset_zt.getMap();
		}
		
		
		Dataset dataset_ssxx= Sys.query("interf_ssyxcsjk","ssyxcsjkid,sbbh,sjsj,zzzsz_s,zzplz,zzfzz,zjsdz_f,xtdqsj,xaxis,yaxis,zaxis", "sbbh = ? " , null,  new Object[]{sbbh });
		
		if(dataset_ssxx.getList().size()<=0)
		{
			mapsbjk.put("msg", "error");
			
		}else{
			mapsbjk.put("msg", "success");
			mapsbjk.put("zzzsz_s", dataset_ssxx.getList().get(0).get("zzzsz_s"));
			mapsbjk.put("zzplz", dataset_ssxx.getList().get(0).get("zzplz"));
			mapsbjk.put("zzfzz", dataset_ssxx.getList().get(0).get("zzfzz"));
			mapsbjk.put("zjsdz_f", dataset_ssxx.getList().get(0).get("zjsdz_f"));
			mapsbjk.put("xtdqsj", dataset_ssxx.getList().get(0).get("xtdqsj"));
			mapsbjk.put("xaxis", dataset_ssxx.getList().get(0).get("xaxis"));
			mapsbjk.put("yaxis", dataset_ssxx.getList().get(0).get("yaxis"));
			mapsbjk.put("zaxis", dataset_ssxx.getList().get(0).get("zaxis"));
		}
		
		Dataset dataset_gzxx= Sys.query("interf_bj","bjid,sbbh,sjsj,xtdqsj,gzm", "sbbh = ?  and sjsj = ?" , null,  val.toArray());
		if(dataset_gzxx.getList().size()<=0)
		{
			mapsbjk.put("gzxx", "error");
			
		}else{
			mapsbjk.put("gzxx", "success");
			mapsbjk.put("gzm", dataset_gzxx.getList().get(0).get("gzm"));
			mapsbjk.put("gzfssj", dataset_gzxx.getList().get(0).get("sjsj"));
		}
		
		bundle.put("mapsbjk", mapsbjk);
	}
	
	public void ifService_ssxxBysbbh(Parameters parameters, Bundle bundle) throws Exception {
		String val_sb = parameters.getString("val_sb"); 
		Date querytime = parameters.getDate("querytime");
		//Dataset dataset_sbzt= Sys.query("interf_sbzthzb"？,"sbbh,max(ztkssj)", "ztkssj < ? sbbh in " + val_sb, " sbbh ",null,  new Object[]{querytime});
		Dataset dataset_sbzt= Sys.query("interf_sbztjk","sbbh,max(sjkssj)", "sjkssj < ? sbbh in " + val_sb, " sbbh ",null,  new Object[]{querytime});
		List<Map<String, Object>> sbzt = dataset_sbzt.getList();
		String con = "";
		for (int i = 0; i < sbzt.size(); i++) {
			if(i!=0)
			{
				con = con +" or ";
			}
			con = con +" ( sbbh = " +sbzt.get(i).get("sbbh") +" and ztkssj = "+sbzt.get(i).get("ztkssj")+" ) ";
		}
		//Dataset dataset_zt= Sys.query("interf_sbzthzb"？,"sbzthzid,sbbh,sbztdm,ztkssj,ztjssj,xtdqsj", con , null,  new Object[]{});
		Dataset dataset_zt= Sys.query("interf_sbztjk","sbztjkid,sbbh,sbztdm,sjkssj,sjjssj,xtdqsj", con , null,  new Object[]{});
		bundle.put("sbzt", dataset_zt.getList());
		
	}

}







