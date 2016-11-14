package com.isesol.mes.ismes.interf.activity;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.mes.ismes.interf.cumtomexception.DynamicTablesException;
import com.isesol.mes.ismes.interf.cumtomexception.ModifyFormatException;
import com.isesol.mes.ismes.interf.cumtomexception.StatusException;

import net.sf.json.JSONObject;

public class DataAccessActivity {

	private static Logger logger = Logger.getLogger(DataAccessActivity.class);
	
	/**
	 * 处理除了报工以外的其他操作
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String insertAccessData(Parameters parameters, Bundle bundle) {
		String tablename = returnStr(parameters.get("tablename"));
		String sbbh = returnStr(parameters.get("sbbh"));
		String json = returnStr(parameters.get("convert_concent"));
		String updateTablesbyid_arr = returnStr(parameters.get("updateTablesbyid_arr"));
		String datefields = returnStr(parameters.get("datefields"));
		String dateFormats = returnStr(parameters.get("dateFormats"));
		String uuid = returnStr(parameters.get("uuid"));
		
		logger.info(" DataAccessActivity insertAccessData ,tablename ==" + tablename +";;;" + 
				"sbbh ==" + sbbh +";;;" +
				"json ==" + json +";;;" + 
				"updateTablesbyid_arr ==" + updateTablesbyid_arr +";;;" + 
				"datefields ==" + datefields +";;;" + 
				"dateFormats ==" + dateFormats +";;;" + 
				"uuid ==" + uuid +";;;" );
		
		JSONObject jsonObject = JSONObject.fromObject(json);
		Map<String, Object> tmpMap = (Map<String, Object>) JSONObject.toBean(jsonObject, HashMap.class);
		try {
			modifyFormat(datefields, dateFormats, tmpMap);
			updateTmpMap(tablename, tmpMap);
			tmpMap.put("xtdqsj", new Date());
			tmpMap.put("sbbh", sbbh);
			tmpMap.remove("uuid");
			int count = 0;
//			// 判断状态是否有改变
//			if (tablename.equals(CustomConstant.设备状态监控表)) {
//				try {
//					checkStatusChange(tmpMap, CustomConstant.设备状态监控表, parameters);
//				} catch (DynamicTablesException e) {
//					e.printStackTrace();
//				}
//			}
			
			if ("".equals(updateTablesbyid_arr)) {
				count = Sys.insert(tablename, tmpMap);
				logger.info("uuid:" + uuid + ";;count 插入数量" + count);
			} else {
				String[] updateTable_arr = updateTablesbyid_arr.split(",");
				int flag = 1; // 判断是更新还是添加
				for (int i = 0; i < updateTable_arr.length; i++) {
					// 先查询
					String temptablename = updateTable_arr[i].split("-")[0];
					if (temptablename.equals(tablename)) {
						flag = 0;
						String keyid = updateTable_arr[i].split("-")[1];
						// 查询在更新
						count = getInfobyKey(tablename, keyid, sbbh);
						if (count > 0) {
							count = Sys.update(tablename, tmpMap, keyid + "=?", sbbh);
							logger.info("uuid:" + uuid + ";;count 更新数量" + count);
						} else {
							count = Sys.insert(tablename, tmpMap);
							logger.info("uuid:" + uuid + ";;count 插入数量" + count);
						}
					}
				}
				if (flag == 1) {
					int insertcount = Sys.insert(tablename, tmpMap);
					logger.info("uuid:" + uuid + ";;insertcount 插入数量" + insertcount);
					boolean b = false;
					if(tablename.equals("interf_sbztjk")){
						b = updateSsTable(parameters, bundle);
					}
					logger.info("uuid:" + uuid + ";;b==" + b);
					if(b){
						addDynamicTables(tmpMap, parameters, tmpMap.get("sbztdm").toString());
					}
				}
			}
		} catch (ModifyFormatException e) {
			e.printStackTrace();
			bundle.put("ErrorMsg", "传递字段异常");
		} catch (StatusException e) {
			e.printStackTrace();
			bundle.put("ErrorMsg", "状态异常");
		}
		// catch (Exception e) {
		// e.printStackTrace();
		// bundle.put("ErrorMsg", "系统异常");
		// }
		return null;
	}

//	private void checkStatusChange(Map<String, Object> tmpMap, String tablename, Parameters parameters)
//			throws DynamicTablesException {
//		Dataset dataset = Sys.query(tablename, "sbztdm",
//				" sbbh=? and sjkssj = (select Max(sjkssj) from interf_interf_sbztjk where sbbh=? ) ", null, null,
//				new Object[] { tmpMap.get("sbbh").toString(), tmpMap.get("sbbh").toString() });
//
//		List<Map<String, Object>> hzList = (List<Map<String, Object>>) dataset.getList();
//		Map<String, Object> hzMap = new HashMap<String, Object>();
//		if (null != hzList && hzList.size() > 0) {
//			if (!tmpMap.get("sbztdm").toString().equals(hzList.get(0).get("sbztdm").toString())) {
//				hzMap.clear();
//				updateHzMap(hzMap, tmpMap);
//				updateHzInfo(hzMap, tmpMap.get("sbbh"), CustomConstant.设备状态汇总表);
//				hzMap.clear();
//				insertHzMap(hzMap, tmpMap);
//				addHzInfo(hzMap, CustomConstant.设备状态汇总表);
//				try {
//					addDynamicTables(hzMap, parameters, tmpMap.get("sbztdm").toString());
//				} catch (Exception e) {
//					throw new DynamicTablesException("调用动态表格出错");
//				}
//
//			}
//		} else {
//			insertHzMap(hzMap, tmpMap);
//			addHzInfo(hzMap, CustomConstant.设备状态汇总表);
//			try {
//				addDynamicTables(hzMap, parameters, tmpMap.get("sbztdm").toString());
//			} catch (Exception e) {
//				throw new DynamicTablesException("调用动态表格出错");
//			}
//
//		}
//	}

//	private void updateHzMap(Map<String, Object> hzMap, Map<String, Object> tmpMap) {
//		// TODO Auto-generated method stub
//		hzMap.put("ztjssj", tmpMap.get("sjkssj"));
//	}

	// private void updateHzInfo(Map<String, Object> hzMap, Object id, String
	// tablename) {
	// // TODO Auto-generated method stub
	// int count = Sys.update(tablename, hzMap,
	// " sbbh=? and ztkssj = (select Max(ztkssj) from interf_interf_sbzthzb) ",
	// id);
	// System.out.println("更新数量" + count);
	// }

//	private void updateHzInfo(Map<String, Object> hzMap, Object id, String tablename) {
//		// TODO Auto-generated method stub
//		int count = Sys.update(tablename, hzMap,
//				"  sbbh=? and ztkssj = (select Max(ztkssj) from interf_interf_sbzthzb where sbbh=?)  ",
//				new Object[] { id, id });
//		System.out.println("更新数量" + count);
//	}

//	private void insertHzMap(Map<String, Object> hzMap, Map<String, Object> tmpMap) {
//		// TODO Auto-generated method stub
//		hzMap.clear();
//		hzMap.put("sbbh", tmpMap.get("sbbh"));
//		hzMap.put("sbztdm", tmpMap.get("sbztdm"));
//		hzMap.put("ztkssj", tmpMap.get("sjkssj"));
//		// hzMap.put("ztjssj", "");
//		hzMap.put("xtdqsj", new Date());
//	}

//	private void addHzInfo(Map<String, Object> hzMap, String tablename) {
//		// TODO Auto-generated method stub
//		int count = Sys.insert(tablename, hzMap);
//		System.out.println("插入数量" + count);
//	}

	private void updateTmpMap(String tablename, Map<String, Object> tmpMap) throws StatusException {
		if ("interf_sbztjk".equals(tablename)) {
			int str = 0;
			if (tmpMap.get("cwzt").toString().equals("0")) {
				str = 50;
			}
			if (tmpMap.get("cwzt").toString().equals("2") || tmpMap.get("cwzt").toString().equals("3")
					|| tmpMap.get("cwzt").toString().equals("4")) {
				str = 30;
			}
			if (tmpMap.get("cwzt").toString().equals("1")) {
				str = 10;
			}
			if (tmpMap.get("cwzt").toString().equals("5")) {
				if (tmpMap.get("xtzt").toString().equals("0")) {
					str = 10;
				} else {
					str = 20;
				}
			}
			tmpMap.put("sbztdm", str);
			if (0 == str) {
				throw new StatusException("传输状态异常");
			}
		}
	}

	public void querytablename(Parameters parameters, Bundle bundle) {
		String keyid = returnStr(parameters.get("type"));
		Dataset dataset = Sys.query("interf_typeTotable", "interftablename", "interftype=?", null, null,
				new Object[] { keyid });
		logger.info("*********DataAccessActivity, querytablename, type = " + keyid+ "*************");
		logger.info(dataset.getCount());
		if (dataset.getList().size() == 1) {
			bundle.put("interftablename", dataset.getMap().get("interftablename"));
		} else {
			bundle.put("interftablename", "");
		}
		
	}

	private int getInfobyKey(String temptablename, String keyid, String value) {
		// TODO Auto-generated method stub
		Dataset dataset = Sys.query(temptablename, keyid, keyid + "=?", null, null, value);
		return dataset.getList().size();
	}

	public void modifyFormat(String datefields, String dateFormats, Map<String, Object> tmpMap)
			throws ModifyFormatException {
		if (!"".equals(datefields)) {
			String[] datefieldarr = datefields.split(",");
			String[] dateFormatarr = dateFormats.split(",");
			for (int i = 0; i < datefieldarr.length; i++) {
				if (tmpMap.containsKey(datefieldarr[i])) {
					String formstr = dateFormatarr[i];
					SimpleDateFormat sdf = new SimpleDateFormat(formstr);
					try {
						tmpMap.put(datefieldarr[i], sdf.parse((String) tmpMap.get(datefieldarr[i])));
					} catch (ParseException e) {
						throw new ModifyFormatException("传递字段异常");
					}
				}
			}
		}
	}

	public String returnStr(Object obj) {
		if (null != obj) {
			return (String) obj;
		} else {
			return "";
		}
	}

	public void bg_5004(Parameters parameters, Bundle bundle) {
		//设备编号
		String sbbh =  parameters.getString("sbbh");
		//程序名称
		String pass_cxmc =  parameters.getString("pass_cxmc");
		String pl_parcount =  parameters.getString("pl_parcount");
		String uuid = parameters.getString("uuid");
		
		logger.info("sbbh==="+sbbh+";;;pass_cxmc==="+pass_cxmc+";;;pl_parcount==="+pl_parcount+";;;" +
				"uuid==="+uuid+";;;");
		
		//工序
		Map<String,Object> gxxxMap = null;
		String gxid = "";
		Map<String,Object> cxxxMap = null;
		//工序组
		Map<String,Object> gxzMap = null;
		String gxzid = "";
		//设备
		Map<String, Object> sbxxMap = null;
		String sbid = "";
		//加工单元
		Map<String,Object> jgdyMap = null;
		String jgdyid = "";
		//工单
		Map<String,Object> gdxxMap = null;
		String gdid = "";
		
		//生产任务
		Map<String,Object> scrwMap = null;
		String scrwpcid = "";
		
		int count = 0;
		int flag = 0;
		
		//工序
		if(flag == 0 ){
			if(pass_cxmc.contains("/") && pass_cxmc.contains(".") && pass_cxmc.indexOf("/") < pass_cxmc.indexOf("."))
				pass_cxmc = pass_cxmc.substring(pass_cxmc.lastIndexOf("/") + 1, pass_cxmc.lastIndexOf("."));
			//根据程序名称得到工序信息
			//根据盒子发送的程序名称 查询 工序表中的程序
			Parameters p_cxxx = new Parameters();
			p_cxxx.set("cxmc_eq", pass_cxmc);
			Bundle bundle_cxxx = Sys.callModuleService("pm", "queryCxxxByparam", p_cxxx);
			cxxxMap = (Map<String, Object>) bundle_cxxx.get("cxxx");
			if( MapUtils.isNotEmpty(cxxxMap)){
				gxid = cxxxMap.get("gxid").toString();
				logger.info("uuid===" + uuid +";;;gxid===" + gxid);
				
				Parameters p_gxxx = new Parameters();
				p_gxxx.set("gxid", gxid);
				Bundle bundle_gxxx = Sys.callModuleService("pm", "pmservice_query_gxxx", p_cxxx);
				gxxxMap = (Map<String, Object>) bundle_gxxx.get("gxxx");
				if(MapUtils.isEmpty(gxxxMap)){
					flag = 2;
				}
			}else{
				flag = 2;
			}
		}
		
		//工序组
		if(flag == 0 ){
			Parameters p_gxz = new Parameters();
			p_gxz.set("gxid", gxid);
			Bundle bundle_gxz = Sys.callModuleService("pm", "pmservice_query_gxz", p_gxz);
			gxzMap = (Map<String, Object>) bundle_gxz.get("gxz");
			if( MapUtils.isNotEmpty(gxzMap)){
				gxzid = gxzMap.get("gxzid").toString();
				logger.info("uuid===" + uuid +";;;gxzid===" + gxzid);
			}else{
				logger.info("uuid===" + uuid + "flag =" + flag);
				flag = 2;
			}
		}
		
		//设备
		if(flag == 0){
			Parameters p_sbxx = new Parameters();
			p_sbxx.set("sbbh", sbbh);
			//根据设备编号得到设备信息
			Bundle bundle_sbxx = Sys.callModuleService("em", "emservice_sbxxList", p_sbxx);
			sbxxMap = (Map<String, Object>) bundle_sbxx.get("sbxx");
			if(MapUtils.isNotEmpty(sbxxMap)){
				sbid = sbxxMap.get("sbid").toString();
				logger.info("uuid===" + uuid +";;;sbid===" + sbid);
			}else{
				logger.info("uuid===" + uuid + "flag =" + flag);
				flag = 1;
			}
			
		}
		
		//加工单元
		if(flag == 0){
			//根据设备信息得到设备加工单元
			Parameters p_jgdy = new Parameters();
			p_jgdy.set("sbid", sbid);
			Bundle bundle_jgdy = Sys.callModuleService("em", "emservice_jgdyBySblxids", p_jgdy);
			List<Map<String, Object>> rows  = (List<Map<String, Object>>) bundle_jgdy.get("data");
			if(CollectionUtils.isNotEmpty(rows)){
				jgdyMap = rows.get(0);
				jgdyid = jgdyMap.get("jgdyid").toString();
				logger.info("uuid===" + uuid +";;;jgdyid===" + jgdyid);
			}else{
				logger.info("uuid===" + uuid + "flag =" + flag);
				flag = 3;
			}
		}
		
		//工单   使用加工单元id的得到工单信息
		if(flag == 0){
			Parameters p_gdxx = new Parameters();
			p_gdxx.set("sbid", jgdyid);
			Bundle gdbundle = Sys.callModuleService("pl", "plservice_query_gdxxBysbid", p_gdxx);
			List<Map<String, Object>> gdxxList = (List<Map<String, Object>>) gdbundle.get("gdxxList");
			if(CollectionUtils.isNotEmpty(gdxxList)){
				gdxxMap = gdxxList.get(0);
				gdid = gdxxMap.get("gdid").toString();
				scrwpcid = gdxxMap.get("pcid").toString();
				logger.info("uuid===" + uuid +";;;gdid===" + gdid);
				logger.info("uuid===" + uuid +";;;pcid===" + scrwpcid);
			}else{
				logger.info("uuid===" + uuid + "flag =" + flag);
				flag = 4;
			}
		}
		
		//任务 批次
		if(flag == 0){
			Parameters p_rwpc = new Parameters();
			p_rwpc.set("scrwpcid", scrwpcid);
			Bundle scrwxxBundle = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService",p_rwpc);
			scrwMap = (Map<String, Object>) scrwxxBundle.get("scrwandpc");
		}
		
		//自动报工，报工流水
		if(flag == 0 ){
			//写入流水表
			Parameters p_bgls = new Parameters();
			p_bgls.set("pc_remark", pl_parcount);
			p_bgls.set("gdid", gdid);
			p_bgls.set("sbid", sbid);
			p_bgls.set("jgdyid", jgdyid);
			Bundle bglsbundle = Sys.callModuleService("pc", "pcservice_addBgls", p_bgls);
			Boolean nc_data_update_flag = (Boolean) bglsbundle.get("nc_data_update_flag");
			if(!nc_data_update_flag){
				flag = 5;
				logger.info("uuid===" + uuid + "flag =" + flag);
			}
		}
		
		if(flag == 0 ){
			//通过工序得到工步，得到刀位
			Parameters gb_parameters = new Parameters();
			gb_parameters.set("gxid", gxid);
			Bundle gbxxbundle = Sys.callModuleService("pm", "pmservice_gbxx",gb_parameters);
			List<Map<String,Object>> gbxxList = (List<Map<String, Object>>) gbxxbundle.get("gbxx");
			for(Map<String,Object> m : gbxxList){
				if(m.get("dw") == null || "".equals(m.get("dw").toString())){
					continue;
				}
				//刀具寿命
				String dw = m.get("dw").toString();
				Parameters js_parameters = new Parameters();
				js_parameters.set("dw", dw);
				js_parameters.set("sbid", sbid);
//				js_parameters.set("wlid", sbid);
//				js_parameters.set("gbid", m.get(key));
				Sys.callModuleService("em", "emservice_sbdwjs",js_parameters);
			}
		}
		
		if(flag == 0 ){
			//修改工单表  nc自动报工数量  
			//从工序的纬度出发，每个工序组的最后一道序完成的时候，进行报工
			Parameters gx_list_parameter = new Parameters();
			gx_list_parameter.set("gxzid", gxzid);
			Bundle gx_list_bundle = Sys.callModuleService("pm", "pmservice_querygxxxbygxzid",gx_list_parameter);
			//得到这个工序组下的所有的工序,一定不为空
			List<Map<String,Object>> gxxxlist = (List<Map<String, Object>>) gx_list_bundle.get("gxxxlist");
			Map<String,Object> last_gxxx = gxxxlist.get(gxxxlist.size()-1);
			//如果当前是最后一道序，进行报工 TODO
			if(gxid.equals(last_gxxx.get("gxid").toString())){
			//if(true){
				Parameters p_update = new Parameters();
				p_update.set("gdid", gdid);
				Bundle updateGdbundle = Sys.callModuleService("pl", "plservice_DataA_UpdateGd",p_update);
				count = (Integer) updateGdbundle.get("count");
			}
		}
		
		sendMessage(sbbh,pass_cxmc,flag, gxxxMap, gxzMap, sbxxMap, jgdyMap, gdxxMap, scrwMap);
		bundle.put("count", count);
	}
	
	public void sendMessage(String sbbh,//盒子传的设备编号
			String pass_cxmc,//盒子传的程序名称
			int flag,Map<String,Object> gxxxMap,//工序
			Map<String, Object> gxzMap,//工序组
			Map<String, Object> sbxxMap,//设备
			Map<String,Object> jgdyMap,//加工单元
			Map<String,Object> gdxxMap,//工单
			Map<String,Object> scrwMap  ){//生产任务
		
		if(flag == 0){
			Map<String, Object> data = new HashMap<String, Object>();
			
			if(scrwMap != null){
				data.put("pcbh",scrwMap.get("pcbh"));// 批次编号
				data.put("pcmc",scrwMap.get("pcmc"));// 批次编号
				data.put("scrwbh", scrwMap.get("scrwbh"));// 生产任务编号
			}
			
			data.put("gxmc", gxxxMap.get("gxmc"));// 工序名称
			data.put("gxzmc", gxzMap.get("gxzmc"));// 工序组名称
			
			
			long starttime = ((java.sql.Timestamp)gdxxMap.get("jhkssj")).getTime();
			long endtime = ((java.sql.Timestamp) gdxxMap.get("jhjssj")).getTime();
			int jgsl = (Integer) gdxxMap.get("jgsl");
			int ncbgsl = (Integer)gdxxMap.get("ncbgsl");
			long nowtime = new Date().getTime();
			//截至到当前  应该完成的数量
			int ygwcsl=  (int) Math.round((nowtime - starttime)  / ( endtime - starttime) * jgsl);
			//截至到当前  应该完成的数量百分比
			double b = new BigDecimal(ncbgsl).divide(new BigDecimal(ygwcsl), 4,BigDecimal.
					ROUND_HALF_UP).multiply(new BigDecimal(100)).doubleValue();
			data.put("sfzcjh", b + "%");
			
			Parameters lj_parameters = new Parameters();
			lj_parameters.set("ljid", gdxxMap.get("ljid"));
			Bundle ljbundle = Sys.callModuleService("pm", "pmservice_query_ljxxFile", lj_parameters);
			Map<String, Object> ljmap = (Map<String, Object>) ljbundle.get("ljtpxx");
			String ljmc = "";
			String ljbh = "";
			if (null != ljmap && ljmap.size() > 0) {
				data.put("ljtp", ljmap.get("url").toString());
				ljbh = ljmap.get("ljbh").toString();
				ljmc = ljmap.get("ljmc").toString();
			}
			
			// 查询任务完成进度 TODO
			Parameters progressCondition = new Parameters();
//			progressCondition.set("scrwbh", scrwMap.get("scrwMap"));
//			Bundle resultProgress = Sys.callModuleService("pc", "pcservice_caculateProgress",
//					progressCondition);
//			Object scrwjd = resultProgress.get("scrwjd");
//			data.put("scrwjd", scrwjd);
			// sendActivity
			String activityType = "0"; // 动态任务
			String templateId = "zdbg_tp";
			String[] roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE", "WORKER_ROLE" };// 生产管理,物料配送
			
			data.put("gdbh", gdxxMap.get("gdbh"));// 工单编号
			data.put("sbbh", sbxxMap.get("sbbh"));// 设备编号
			data.put("ljbh", ljbh);// 零件编号
			data.put("ljmc", ljmc);// 零件名称
			data.put("gdybgsl", gdxxMap.get("gdybgsl").toString());// 工人报工数量
			data.put("jgsl", gdxxMap.get("jgsl"));// 计划加工数量
			
			data.put("jhkssj", ((java.sql.Timestamp)gdxxMap.get("jhkssj")).getTime());// 计划开始时间
			data.put("jhjssj", ((java.sql.Timestamp)gdxxMap.get("jhjssj")).getTime());// 计划结束时间
			data.put("bcbgsl", 1);// 本次报工数量
			int sum = Integer.parseInt(gdxxMap.get("ncbgsl").toString()) + 1;
			data.put("ncbgsl", sum);// NC自动报工数量
			
			data.put("userid", Sys.getUserIdentifier());// 操作人
			data.put("username", Sys.getUserName());// 操作人
			sendActivity(activityType, templateId, true, roles, null, null, data);
		}
		else{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日  HH时mm分");
			String message_type = "0";// 报警
			String url = "";//Sys.getAbsoluteUrl("");
			Map<String, Object> message_data = new HashMap<String, Object>();
			Date now = new Date();
			String bizType = "nczdbg";// nc自动报工
			String[] message_roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE" };// 生产管理
			String bizId = sbbh;
			StringBuffer content = new StringBuffer();
			StringBuffer title = new StringBuffer();
			if(flag == 1){
				// 标题：**设备报工工单与开工工单不匹配
				// 正文：**年**月**日*时*分，**设备的报工工单与开工工单不匹配，请核查。
				title.append(sbbh).append("设备报工工单与开工工单不匹配");
				content.append(sdf.format(now)).append(sbbh).append("设备的报工工单与开工工单不匹配，请核查。");
				sendMessage(message_type, title.toString(), null, content.toString(), "系统", bizType, bizId,
						"0"/* 信息优先级：0:一般，1：紧急 ， 2：非常紧急 */, url, message_roles, null, null,
						"system"/* manual:人工发送，system：系统发送，interface：外部接口 */, "if"/* 消息来源ID */, message_data, now, null,
						null, null);
			}
			if(flag == 2){
				// 标题：**设备程序与系统程序名称不匹配
				// 正文：**年**月**日*时*分，**设备程序与系统程序名称不匹配，请核查。
				title.append(sbbh).append("设备程序与系统程序名称不匹配");
				content.append(sdf.format(now)).append(sbbh).append("设备程序与系统程序名称不匹配，请核查。");
				sendMessage(message_type, title.toString(), null, content.toString(), "系统", bizType, bizId,
						"0"/* 信息优先级：0:一般，1：紧急 ， 2：非常紧急 */, url, message_roles, null, null,
						"system"/* manual:人工发送，system：系统发送，interface：外部接口 */, "if"/* 消息来源ID */, message_data, now, null,
						null, null);
			}
			if(flag == 3){
				// 标题：**设备所属加工单元异常
				// 正文：**年**月**日*时*分，**设备所属加工单元异常，请核查。
				title.append(sbbh).append("设备所属加工单元异常");
				content.append(sdf.format(now)).append(sbbh).append("设备所属加工单元异常，请核查。");
				sendMessage(message_type, title.toString(), null, content.toString(), "系统", bizType, bizId,
						"0"/* 信息优先级：0:一般，1：紧急 ， 2：非常紧急 */, url, message_roles, null, null,
						"system"/* manual:人工发送，system：系统发送，interface：外部接口 */, "if"/* 消息来源ID */, message_data, now, null,
						null, null);
			}
			if(flag == 4){
				
			}
			if(flag == 5){
				
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public String bg_5004_xiaodong(Parameters parameters, Bundle bundle) {
		String sbbh = (String) parameters.get("sbbh");
		String pass_cxmc = (String) parameters.get("pass_cxmc");
		String pl_parcount = (String) parameters.get("pl_parcount");
		// sbbh = "A131420096";
		// pass_cxmc = "/home/fiyang/program/Z44231001_001.iso";

		long sjtime = new Date().getTime();
		parameters.set("sbbh", sbbh);
		parameters.set("sjtime", sjtime);
		Bundle sbxxListbundle = Sys.callModuleService("em", "emservice_sbxxList", parameters);
		List<Map<String, Object>> sbxxList = (List<Map<String, Object>>) sbxxListbundle.get("sbxxList");
		String sbid = "";
		int count = 0;
		int flag = 0;
		if (null != sbxxList && sbxxList.size() > 0) {
			sbid = sbxxList.get(0).get("sbid").toString();
			parameters.set("sbid", sbid);
			Bundle gdbundle = Sys.callModuleService("pl", "plservice_query_gdxxBysbid", parameters);

			List<Map<String, Object>> gdxxList = (List<Map<String, Object>>) gdbundle.get("gdxxList");
			if (null != gdxxList && gdxxList.size() > 0) {
				String gxid = gdxxList.get(0).get("gxid").toString();
				parameters.set("gxid", gxid);
				parameters.set("gdid", gdxxList.get(0).get("gdid").toString());
				parameters.set("zxbz", 1);

				Bundle bglsbundle = Sys.callModuleService("pc", "pcservice_addBgls", parameters);

				Bundle cxbundle = Sys.callModuleService("pm", "pmservice_cxxxbysbgx", parameters);
				List<Map<String, Object>> cxxx = (List<Map<String, Object>>) cxbundle.get("cxxx");
				if (null != cxxx && null != cxxx.get(0).get("cxmczd")) {
					String cxmczd = cxxx.get(0).get("cxmczd").toString();
					if (cxxx.get(0).get("cxmczd").toString().contains(".")) {
						cxmczd = cxxx.get(0).get("cxmczd").toString().substring(0,
								cxxx.get(0).get("cxmczd").toString().lastIndexOf("."));
					}
					if (pass_cxmc.substring(pass_cxmc.lastIndexOf("/") + 1, pass_cxmc.lastIndexOf("."))
							.contains(cxmczd)) {
						// if (true) {
						Bundle gxxxbundle = Sys.callModuleService("pm", "queryGxxxByGxid", parameters);
						Map<String, Object> gxxx = (Map<String, Object>) gxxxbundle.get("gxxx");
						if (null != gxxx && gxxx.size() > 0) {
							if (gxxx.get("zlbj").equals("10")) {
								// 必检
								Bundle updateBgbundle = Sys.callModuleService("pl", "plservice_updateNcbgsl",
										parameters);
								count = (Integer) updateBgbundle.get("count");
							} else {
								// 非必检
								Bundle updateGdbundle = Sys.callModuleService("pl", "plservice_DataA_UpdateGd",
										parameters);
								count = (Integer) updateGdbundle.get("count");
							}

							// parameters.set("ljid",
							// gdxxList.get(0).get("ljid"));
							// Bundle ljbundle = Sys.callModuleService("pm",
							// "pmservice_query_ljxxFile", parameters);
							// String ljmc = "";
							// String ljbh = "";
							// Map<String, Object> ljmap = (Map<String, Object>)
							// ljbundle.get("ljtpxx");
							// if (null != ljmap && ljmap.size() > 0) {
							// ljbh = ljmap.get("ljbh").toString();
							// ljmc = ljmap.get("ljmc").toString();
							// }
							Map<String, Object> data = new HashMap<String, Object>();

							parameters.set("pcid", gdxxList.get(0).get("pcid"));
							Bundle pcbundle = Sys.callModuleService("pro", "proserver_scrwpcListByPcidService",
									parameters);
							List<Map<String, Object>> pcList = (List<Map<String, Object>>) pcbundle.get("scrwpcList");
							if (null != pcList && pcList.size() > 0) {
								data.put("pcbh", pcList.get(0).get("pcbh"));// 批次编号
								data.put("pcmc", pcList.get(0).get("pcmc"));// 批次编号
							}

							Bundle gxbundle = Sys.callModuleService("pm", "queryGxxxByGxid", parameters);
							Map<String, Object> gx = (Map<String, Object>) gxbundle.get("gxxx");
							if (null != gx && gx.size() > 0) {
								data.put("gxmc", pcList.get(0).get("gxmc"));// 工序名称
							}

							Bundle sfzcjhbundle = Sys.callModuleService("pl", "plservice_queryGdxxByGdSbId",
									parameters);
							String sfzcjh = (String) sfzcjhbundle.get("sfzcjh");

							// 查询任务编号
							parameters.set("scrwpcid", gdxxList.get(0).get("pcid"));
							Bundle scrwxxBundle = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService",
									parameters);
							List<Map<String, Object>> scrwxx = (List<Map<String, Object>>) scrwxxBundle
									.get("scrwandpcList");
							Object scrwbh = scrwxx.get(0).get("scrwbh");
							data.put("scrwbh", scrwbh);// 生产任务编号
							// //查询零件图片
							// parameters.set("ljid",
							// pcList.get(0).get("ljid"));
							// Bundle resultLjUrl = Sys.callModuleService("pm",
							// "partsInfoService", parameters);
							// Object ljtp =
							// ((Map)resultLjUrl.get("partsInfo")).get("url");
							// data.put("ljtp", ljtp);

							// parameters.set("ljid",
							// gdxxList.get(0).get("ljid"));
							// Bundle ljbundle = Sys.callModuleService("pm",
							// "pmservice_query_ljxxFile", parameters);
							// String ljmc = "";
							// String ljbh = "";
							// Map<String, Object> ljmap = (Map<String, Object>)
							// ljbundle.get("ljtpxx");
							// if (null != ljmap && ljmap.size() > 0) {
							// ljbh = ljmap.get("ljbh").toString();
							// ljmc = ljmap.get("ljmc").toString();
							// }

							String ljmc = "";
							String ljbh = "";
							parameters.set("ljid", gdxxList.get(0).get("ljid"));
							Bundle ljbundle = Sys.callModuleService("pm", "pmservice_query_ljxxFile", parameters);
							Map<String, Object> ljmap = (Map<String, Object>) ljbundle.get("ljtpxx");
							if (null != ljmap && ljmap.size() > 0) {
								// returnmap.put("ljurl", ljmap.get("url"));
								// returnmap.put("ljbh", ljmap.get("ljbh"));
								// returnmap.put("ljmc", ljmap.get("ljmc"));
								data.put("ljtp", ljmap.get("url").toString());
								ljbh = ljmap.get("ljbh").toString();
								ljmc = ljmap.get("ljmc").toString();
							}

							// 查询任务完成进度
							Parameters progressCondition = new Parameters();
							progressCondition.set("scrwbh", scrwbh);
							Bundle resultProgress = Sys.callModuleService("pc", "pcservice_caculateProgress",
									progressCondition);
							Object scrwjd = resultProgress.get("scrwjd");
							data.put("scrwjd", scrwjd);
							// sendActivity
							String activityType = "0"; // 动态任务
							String templateId = "zdbg_tp";
							String[] roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE", "WORKER_ROLE" };// 生产管理,物料配送

							data.put("gdbh", gdxxList.get(0).get("gdbh").toString());// 工单编号
							data.put("sbbh", sbbh);// 设备编号
							data.put("ljbh", ljbh);// 零件编号
							data.put("ljmc", ljmc);// 零件名称
							data.put("gdybgsl", gdxxList.get(0).get("gdybgsl").toString());// 工人报工数量
							data.put("jgsl", gdxxList.get(0).get("jgsl"));// 计划加工数量

							data.put("jhkssj", ((java.sql.Timestamp) gdxxList.get(0).get("jhkssj")).getTime());// 计划开始时间
							data.put("jhjssj", ((java.sql.Timestamp) gdxxList.get(0).get("jhjssj")).getTime());// 计划结束时间
							data.put("bcbgsl", 1);// 本次报工数量
							int sum = Integer.parseInt(gdxxList.get(0).get("ncbgsl").toString()) + 1;
							data.put("ncbgsl", sum);// NC自动报工数量
							data.put("sfzcjh", sfzcjh);// 是否遵从计划

							data.put("userid", Sys.getUserIdentifier());// 操作人
							data.put("username", Sys.getUserName());// 操作人
							sendActivity(activityType, templateId, true, roles, null, null, data);

							// //sendMessage
							// SimpleDateFormat sdf = new
							// SimpleDateFormat("yyyy年MM月dd日 HH时mm分");
							// String message_type = "1";// 待办事项
							// String url =
							// Sys.getAbsoluteUrl("/ismes-web/pc/zlqr/query_zlqr?_m=quality_confirm");
							// Map<String, Object> message_data = new
							// HashMap<String, Object>();
							// Date now = new Date();
							// String bizType = "scrwpcxf";// 缺少刀具报警
							// String bizId =
							// gdxxList.get(0).get("gdbh").toString();
							// StringBuffer content = new StringBuffer();
							// StringBuffer title = new StringBuffer();
							// title.append("产品名称（编号为").append(ljbh).append("）的生产工单（编号为").append(ljbh).append("）进行报工");
							// content.append(sdf.format(now)).append(",产品名称（编号为").append("xxx").append("）的生产工单（工单编号为").append("xxx").append("，工序为").append("xxx").append("）进行报工，报工数量1件，请质检部门检验确认。");
							//
							// sendMessage(message_type, title.toString(), null,
							// content.toString(), "系统", bizType, bizId,
							// "0"/* 信息优先级：0:一般，1：紧急 ， 2：非常紧急 */, url, roles,
							// null, null,
							// "system"/* manual:人工发送，system：系统发送，interface：外部接口
							// */, "ts"/* 消息来源ID */, message_data, now, null,
							// null, null);
						}
					} else {
						flag = 1;
					}
				} else {
					flag = 1;
				}
			} else {
				flag = 1;
			}
		}
		if (flag == 1) {
			// 程序名没有匹配上
			// sendMessage
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日  HH时mm分");
			String message_type = "0";// 报警
			String url = Sys.getAbsoluteUrl("");
			Map<String, Object> message_data = new HashMap<String, Object>();
			Date now = new Date();
			String bizType = "nczdbg";// nc自动报工
			String[] message_roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE" };// 生产管理
			String bizId = sbbh;
			StringBuffer content = new StringBuffer();
			StringBuffer title = new StringBuffer();

			// 标题：**设备报工工单与开工工单不匹配
			// 正文：**年**月**日*时*分，**设备的报工工单与开工工单不匹配，请核查。

			title.append(sbbh).append("设备报工工单与开工工单不匹配");
			content.append(sdf.format(now)).append(sbbh).append("设备的报工工单与开工工单不匹配，请核查。");

			sendMessage(message_type, title.toString(), null, content.toString(), "系统", bizType, bizId,
					"0"/* 信息优先级：0:一般，1：紧急 ， 2：非常紧急 */, url, message_roles, null, null,
					"system"/* manual:人工发送，system：系统发送，interface：外部接口 */, "if"/* 消息来源ID */, message_data, now, null,
					null, null);
		}
		bundle.put("count", count);
		return null;
	}

	public String status_timeout(Parameters parameters, Bundle bundle) {
		String sbbh = (String) parameters.get("sbbh");
		Map<String, Object> inter_zt_Map = new HashMap<String, Object>();
//		Map<String, Object> inter_hz_Map = new HashMap<String, Object>();
//		Map<String, Object> update_Hz_Map = new HashMap<String, Object>();
//		Map<String, Object> query_Hz_Map = new HashMap<String, Object>();

		Date d = new Date();
		inter_zt_Map.put("sbbh", sbbh);
		inter_zt_Map.put("sjkssj", d);
		inter_zt_Map.put("sbztdm", 40);
		inter_zt_Map.put("xtdqsj", d);

//		inter_hz_Map.put("sbbh", sbbh);
//		inter_hz_Map.put("sbztdm", 40);
//		inter_hz_Map.put("ztkssj", d);
//		inter_hz_Map.put("xtdqsj", d);

//		update_Hz_Map.put("ztjssj", d);
//		int fsflag = 1;
//		Dataset dataset = Sys.query("interf_sbzthzb", "sbbh,sbztdm",
//				" sbbh=? and ztkssj = (select Max(ztkssj) from interf_interf_sbzthzb where sbbh=?) ", null, null,
//				new Object[] { sbbh, sbbh });
//		if (null != dataset && dataset.getList().size() > 0) {
//			int count = Sys.update("interf_sbzthzb", update_Hz_Map,
//					"  sbbh=? and ztkssj = (select Max(ztkssj) from interf_interf_sbzthzb where sbbh=?)  ",
//					new Object[] { sbbh, sbbh });
//			if (dataset.getList().get(0).get("sbztdm").toString().equals("40")) {
//				fsflag = 0;
//			}
//		}
		int insert_zt_count = Sys.insert("interf_sbztjk", inter_zt_Map);
//		int insert_hz_count = Sys.insert("interf_sbzthzb", inter_hz_Map);

//		if (fsflag == 1) {
//			addDynamicTables(inter_hz_Map, parameters, "40");
//		}

		return null;
	}

	private void addDynamicTables(Map<String, Object> hz_Map, Parameters parameters, String sbzt) {
		// TODO Auto-generated method stub
		String activityType = "1"; // 动态任务
		String templateId = "sbzt_tp";
		String[] roles = new String[] { "MANUFACTURING_MANAGEMENT_ROLE" };// 关注该动态的角色
		Map<String, Object> data = new HashMap<String, Object>();

		parameters.set("val_sb", "('" + hz_Map.get("sbbh") + "')");
		Bundle sbxxbundle = Sys.callModuleService("em", "emservice_sbxxBysbbh", parameters);
		List<Map<String, Object>> gdxxList = (List<Map<String, Object>>) sbxxbundle.get("sbxx");
		if (null != gdxxList && gdxxList.size() > 0) {
			data.put("sbbh", gdxxList.get(0).get("sbbh"));// 设备编号
			data.put("sbmc", gdxxList.get(0).get("sbmc"));// 设备名称
			data.put("sblxid", gdxxList.get(0).get("sblxid"));// 设备类型
			if (sbzt != null) {
				String[] sbztmc = Sys.getDictFieldNames("sb_zt", sbzt);
				if (sbztmc != null && sbztmc.length > 0)
					sbzt = sbztmc[0];
			}
			data.put("sbzt", sbzt);// 设备状态
			// 查询设备图片
			Bundle b_sbxx = Sys.callModuleService("em", "emservice_sbxxFile", parameters);
			Map<String, Object> map_sb = (Map<String, Object>) b_sbxx.get("sbxx");
			data.put("sbtp", map_sb.get("url"));
			data.put("userid", Sys.getUserIdentifier());// 操作人
			data.put("username", Sys.getUserName());// 操作人
			sendActivity(activityType, templateId, true, roles, null, null, data);
		}
	}

	private Bundle sendActivity(String type, String templateId, boolean isPublic, String[] roles, String[] users,
			String[] group, Map<String, Object> data) {
		String PARAMS_TYPE = "type";
		String PARAMS_TEMPLATE_ID = "template_id";
		String PARAMS_PUBLIC = "public";
		String PARAMS_ROLE = "role";
		String PARAMS_USER = "user";
		String PARAMS_GROUP = "group";
		String PARAMS_DATA = "data";
		String SERVICE_NAME = "activity";
		String METHOD_NAME = "send";
		Parameters parameters = new Parameters();
		parameters.set(PARAMS_TYPE, type);
		parameters.set(PARAMS_TEMPLATE_ID, templateId);
		if (isPublic)
			parameters.set(PARAMS_PUBLIC, "1");
		if (roles != null && roles.length > 0)
			parameters.set(PARAMS_ROLE, roles);
		if (users != null && users.length > 0)
			parameters.set(PARAMS_USER, users);
		if (group != null && group.length > 0)
			parameters.set(PARAMS_GROUP, group);
		if (data != null && !data.isEmpty())
			parameters.set(PARAMS_DATA, data);
		return Sys.callModuleService(SERVICE_NAME, METHOD_NAME, parameters);
	}

	private Bundle sendMessage(String type, String title, String abs, String content, String from, String bizType,
			String bizId, String priority, String url, String[] roles, String[] users, String[] group,
			String sourceType, String sourceId, Map<String, Object> data, Date sendTime, String[] fileUri,
			String[] fileNames, long[] filesSize) {
		String PARAMS_TYPE = "message_type";
		String PARAMS_ROLE = "receiver_role";
		String PARAMS_USER = "receiver_user";
		String PARAMS_GROUP = "receiver_group";
		String PARAMS_TITLE = "title";
		String PARAMS_ABSTRACT = "abstract";
		String PARAMS_CONTENT = "content";
		String PARAMS_FROM = "from";
		String PARAMS_DATA = "data";
		String PARAMS_PRIORITY = "priority";
		String PARAMS_SOURCE_TYPE = "source_type";
		String PARAMS_SOURCE_ID = "source_id";
		String PARAMS_URL = "url";
		String PARAMS_FILE_URI = "file_uri";
		String PARAMS_FILE_NAME = "file_name";
		String PARAMS_FILE_SIZE = "file_size";
		String PARAMS_BIZTYPE = "biz_type";
		String PARAMS_BIZID = "biz_id";
		String PARAMS_SEND_TIME = "send_time";
		String SERVICE_NAME = "message";
		String METHOD_NAME = "send";
		Parameters parameters = new Parameters();
		parameters.set(PARAMS_TITLE, title);
		parameters.set(PARAMS_ABSTRACT, abs);
		parameters.set(PARAMS_CONTENT, content);
		parameters.set(PARAMS_FROM, from);
		parameters.set(PARAMS_BIZTYPE, bizType);
		parameters.set(PARAMS_BIZID, bizId);
		parameters.set(PARAMS_TYPE, type);
		parameters.set(PARAMS_PRIORITY, priority);
		parameters.set(PARAMS_USER, users);
		parameters.set(PARAMS_GROUP, group);
		parameters.set(PARAMS_ROLE, roles);
		parameters.set(PARAMS_SOURCE_TYPE, sourceType);
		parameters.set(PARAMS_SOURCE_ID, sourceId);
		parameters.set(PARAMS_URL, url);
		parameters.set(PARAMS_FILE_URI, fileUri);
		parameters.set(PARAMS_FILE_NAME, fileNames);
		parameters.set(PARAMS_FILE_SIZE, filesSize);
		parameters.set(PARAMS_SEND_TIME, sendTime);
		parameters.set(PARAMS_DATA, data);
		return Sys.callModuleService(SERVICE_NAME, METHOD_NAME, parameters);
	}
	
	public boolean updateSsTable(Parameters parameters, Bundle bundle) {
		boolean flag = false;
		String sbbh = parameters.getString("sbbh");
		Dataset dataset = Sys.query("interf_sbztjk", "sbztjkid,sbbh, cwzt, xtdqsj, sbztdm, sjkssj, sjjssj, xtzt",
				"sjjssj is null and sbbh = ? ", null, "sjkssj,xtdqsj", new Object[] {sbbh});
		List<Map<String, Object>> all_nosjjssj_List = dataset.getList();
		List<Object[]> objlist = new ArrayList<Object[]>();
		if (null != all_nosjjssj_List && all_nosjjssj_List.size() > 0) { // 如果汇总表中存在一些设备信息
			for (int i = 1; i < all_nosjjssj_List.size(); i++) {
				int index = i - 1;
				all_nosjjssj_List.get(index).put("sjjssj", all_nosjjssj_List.get(index + 1).get("sjkssj"));
				objlist.add(new Object[] { all_nosjjssj_List.get(index).get("sbztjkid") });
			}
			//判断是不是要发activity
			if(all_nosjjssj_List.size() == 1){
				Map<String,Object> lastMap = all_nosjjssj_List.get(all_nosjjssj_List.size() -1);
				flag = true;
			}
			if(all_nosjjssj_List.size() > 1){
				Map<String,Object> lastMap = all_nosjjssj_List.get(all_nosjjssj_List.size() -1);
				String last_sbztdm = lastMap.get("sbztdm").toString();
				Map<String,Object> last2Map = all_nosjjssj_List.get(all_nosjjssj_List.size() -2);
				String last_2_sbztdm = last2Map.get("sbztdm").toString();
				if(!last_sbztdm.equals(last_2_sbztdm)){
					flag = true;
				}
			}
			
			
			all_nosjjssj_List.remove(all_nosjjssj_List.size() - 1);
			if (all_nosjjssj_List.size() > 0) {
				Sys.update("interf_sbztjk", all_nosjjssj_List, "sbztjkid=?", objlist);
			}
		}
		return flag;
	}
	
	public String insertSsTable(Parameters parameters, Bundle bundle) {
		Map<String, Object> ss_map = new HashMap<String, Object>();
		String timeoutMsg = parameters.getString("timeoutMsg");
		String uuid = parameters.getString("uuid");
		logger.info("METHOD insertSsTable,timeoutMsg = " + timeoutMsg + ";;uuid = " + uuid);
		JSONObject jsStr = JSONObject.fromObject(timeoutMsg);
		//{"encode":false,"id":"","machineNo":"SMTCL_MACHINE_A131420089","type":-1,"content":"rwkrX67WqUxOy7eA"}
		
		Date date = new Date();
		String sbbh = jsStr.get("machineNo").toString().replace("SMTCL_MACHINE_", "");
		ss_map.put("sbbh", sbbh );
		ss_map.put("sjkssj", date);
		ss_map.put("xtdqsj", date);
		ss_map.put("sbztdm", "40");
		int count = Sys.insert("interf_sbztjk", ss_map);
		
		//修改其他的状态
		parameters.set("sbbh",sbbh);
		boolean flag = updateSsTable(parameters, bundle);
		logger.info("METHOD insertSsTable,flag = " + flag + ";;uuid = " + uuid);
		//发送消息
		if(flag){
			addDynamicTables(ss_map, parameters, "40");
		}
		
		return null;
	}
	
//	/**
//	 * 
//	 * @param parameters
//	 * @param bundle
//	 * @return
//	 */
//	public String insertHzInfo(Parameters parameters, Bundle bundle) {
//		String inSbbh = "'";
//		List<Map<String, Object>> update_sbztList = null;
//		//找到汇总表里面每个设备最大数据时间的设备信息
//		Dataset dataset = Sys.query("interf_sbzthzb", "sbbh,max(sjkssj) sjkssj","","sbbh", null,new Object[] {});
//		if(null != dataset && dataset.getList().size()>0){	//如果汇总表中存在一些设备信息
//			inSbbh = updateHz(inSbbh,dataset.getList());	//更新存在的设备信息
//			System.out.println("9");
//			insertSyHz(inSbbh);	//把汇总表中不存在的设备信息保存到汇总表
//		} else {	//如果不存在任何的设备信息
//			insertSyHz(inSbbh);
//		}
//		return null;
//	}
	
//	private void insertSyHz(String inSbbh) {
//		Dataset all_sbztDs;
//		if("'".equals(inSbbh)){	//如果不存在任何的设备信息，那么就把实时表中的所有设备信息都获取到
//			System.out.println("a");
//			all_sbztDs = Sys.query("interf_sbztjk", "sbbh, cwzt, xtdqsj, sbztjkid, sbztdm, sjkssj, xtzt","",null, "sjkssj",new Object[] {});
//		} else {	//如果存在一些设备信息，那么就把除了汇总表中存在的设备信息都获取到
//			System.out.println("b");
//			all_sbztDs = Sys.query("interf_sbztjk", "sbbh, cwzt, xtdqsj, sbztjkid, sbztdm, sjkssj, xtzt","sbbh not in ("+ inSbbh +")",null, "sjkssj",new Object[] {});
//		}
//		if(null != all_sbztDs && all_sbztDs.getList().size()>0){	//如果实时表中查询出信息了
//			System.out.println("c");
//			List<Map<String, Object>> all_sbztList = all_sbztDs.getList();
//			Map<String, String> sbmap = new HashMap<String, String>();
//			for (int i = 0; i < all_sbztList.size(); i++) {
//				System.out.println("d");
//				//获取到每条信息的状态
//				String status = all_sbztList.get(i).get("sbztdm").toString();
//				if(sbmap.containsKey(all_sbztList.get(i).get("sbbh").toString())){	//如果map中存在设备信息
//					System.out.println("e");
//					if((sbmap.get(all_sbztList.get(i).get("sbbh").toString()).equals(status))){//判断map中的设备信息状态是否有改变
//						//如果状态一样的话，不将list的该设备信息保存到汇总表
//						all_sbztList.remove(i);
//						i--;
//						System.out.println("f");
//					} else {
//						//如果状态不一样的话，则list的这条信息保存到hz表中，并把这个设备的信息保存到map中
//						sbmap.put(all_sbztList.get(i).get("sbbh").toString(), status);
//						System.out.println("g");
//					}
//				} else {
//					System.out.println("h");
//					//如果map中不存在设备信息，则list的这条信息保存到hz表中，并把这个设备的信息保存到map中
//					sbmap.put(all_sbztList.get(i).get("sbbh").toString(), status);
//				}
//			}
//			System.out.println("i");
//			//最终保存设备信息
//			Sys.insert("interf_sbzthzb", all_sbztList);
//		}
//	}

//	private String updateHz(String inSbbh,List<Map<String, Object>> update_sbztList) {
//		Dataset update_sbztDs;
//		for (int i = 0; i < update_sbztList.size(); i++) {
//			inSbbh = inSbbh + update_sbztList.get(i).get("sbbh").toString() + "','";
//			System.out.println("1");
//			//在实时表中发现有此设备信息，并且时间大于汇总表里数据时间的
//			update_sbztDs = Sys.query("interf_sbztjk", "sbbh, cwzt, xtdqsj, sbztjkid, sbztdm, sjkssj, xtzt","sbbh in ('"+ update_sbztList.get(i).get("sbbh") +"') and sjkssj >= ?",null, "sjkssj",new Object[] {update_sbztList.get(i).get("sjkssj")});
//			System.out.println("2");
//			if(null != update_sbztDs && update_sbztDs.getList().size()>0){	//如果实时表中有这样的信息
//				System.out.println("3");
//				List<Map<String, Object>> sbztList = update_sbztDs.getList();
//				Map<String, String> sbmap = new HashMap<String, String>();
//				System.out.println("4");
////				Dataset thelasthzinfo = Sys.query("interf_sbztjk", "sbbh,sbztdm, sjkssj","sbbh in ('"+ update_sbztList.get(i).get("sbbh").toString() +"')",null, null,new Object[] {});
//				for (int j = 0; j < sbztList.size(); j++) {
////					System.out.println(sbztList.get(0).get("sbztdm").toString());
////					System.out.println(thelasthzinfo.getList().get(0).get("sbztdm").toString());
////					if(sbztList.get(0).get("sbztdm").toString().equals(thelasthzinfo.getList().get(0).get("sbztdm").toString())){
////						if(sbztList.get(0).get("sbztdm").toString().equals(thelasthzinfo.getList().get(0).get("sbztdm").toString())){
////							continue; 
////						}
////					}
//					System.out.println("5");
//					//获取每一条的状态
//					String status = sbztList.get(i).get("sbztdm").toString();
//					System.out.println("6");
//					//判断map中是否有这样的记录
//					if(sbmap.containsKey(sbztList.get(i).get("sbbh").toString())){	//如果map中存在设备信息
//						System.out.println("7");
//						if((sbmap.get(sbztList.get(i).get("sbbh").toString()).equals(status))){//判断map中的设备信息状态是否有改变
//							//如果状态一样的话，不将list的该设备信息保存到汇总表
//							sbztList.remove(i);
//							i--;
//						} else {
//							//如果状态不一样的话，则list的这条信息保存到hz表中，并把这个设备的信息保存到map中
//							sbmap.put(sbztList.get(i).get("sbbh").toString(), status);
//						}
//					} else {
//						System.out.println("8");
//						//如果map中不存在设备信息，则list的这条信息保存到hz表中，并把这个设备的信息保存到map中
//						sbmap.put(sbztList.get(i).get("sbbh").toString(), status);
//						for (int k = 0; k < update_sbztList.size(); k++) {
//							//如果跟汇总表中最后一条的状态一致得话，则不保存这条信息，删除map中的这个key值，删除list中的这条数据
//							if((update_sbztList.get(k).get("sbbh").toString()).equals(sbmap.get(sbztList.get(i).get("sbbh").toString()))){
//								sbmap.remove(sbztList.get(i).get("sbbh").toString());
//								sbztList.remove(i);
//								i--;
//							}
//						}
//					}
//				}
//				Sys.insert("interf_sbzthzb", sbztList);
//			} 
//		}
//		if(!inSbbh.equals("'")){
//			inSbbh = inSbbh.substring(0, inSbbh.length()-2);
//		} 
//		System.out.println(inSbbh+"%%%%%%%%%%%%%%%%%%%%%%%");
//		return inSbbh;
//	}
	
	/**
	 *  //9001  凡达克的机床  实时数据
		//9002  凡达克的机床  状态变化
		//9003  凡达克的机床  断线
		//9004  报工
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void fdkPostJson(Parameters parameters, Bundle bundle) {
		String type = parameters.getString("type");
		String sbbh = parameters.getString("sbbh");
		String jsonContent = parameters.getString("content");
		
		String uuid = UUID.randomUUID().toString();
		
		logger.info("uuid==" + uuid +";;; type = "+type+";; sbbh = "+sbbh+";;;content==" + jsonContent);
		if(!"9001".equals(type) && !"9002".equals(type) && !"9003".equals(type)&& !"9004".equals(type)){
			logger.info("&&&&&  数据类型推送有异常  type===" + type +";;; uuid = "+uuid+";;;   &&&&&");
		}
		
		if("9001".equals(type)){
			String tablename = "interf_ssyxcsjk";//querytablename(parameters, bundle);
			JSONObject jsonObject = JSONObject.fromObject(jsonContent);
			Map<String, Object> tmpMap = (Map<String, Object>) JSONObject.toBean(jsonObject, HashMap.class);
			tmpMap.put("xtdqsj", new Date());
			tmpMap.put("sbbh", sbbh);
			
			if(tmpMap.get("sjsj") != null && tmpMap.get("sjsj") instanceof String){
				tmpMap.put("sjsj", string2Date(tmpMap.get("sjsj").toString()));
			}
			
			Dataset dataset = Sys.query(tablename, "sbbh", " sbbh = ? ", null, new Object[]{sbbh});
			//如果存在是更新
			if(dataset.getCount() > 0){
				 int count = Sys.update(tablename, tmpMap, " sbbh = ?  ",  new Object[]{sbbh});
				 System.out.println("更新数量" + count);
			}
			//否则是插入
			else{
				int count = Sys.insert("interf_ssyxcsjk", tmpMap);
				System.out.println("插入数量" + count);
			}
		}
		if("9002".equals(type)){
			String tablename = "interf_sbztjk";// querytablename(parameters, bundle);
			JSONObject jsonObject = JSONObject.fromObject(jsonContent);
			Map<String, Object> tmpMap = (Map<String, Object>) JSONObject.toBean(jsonObject, HashMap.class);
			String sbztdm = (String) tmpMap.get("sbztdm");
			
			Dataset dataset = Sys.query(tablename, "sbztjkid,sbbh,sjkssj,sjjssj,sbztdm", " sbbh = ? and sjjssj is null",
					null, new Object[]{sbbh});
			Date now =  new Date();
			if(dataset.getCount() > 0){
				HashMap<String, Object> hashamap = new HashMap<String, Object>();
				hashamap.put("sjjssj", now);
				int count = Sys.update(tablename, hashamap, " sbbh = ? and sjjssj is null " , sbbh);
				System.out.println("更新数量" + count);
			}
			HashMap<String, Object> hashamap = new HashMap<String, Object>();
			hashamap.put("sbbh", sbbh);
			hashamap.put("sjkssj",  now);
			hashamap.put("xtdqsj",  now);
			hashamap.put("sbztdm",  sbztdm);
			int count = Sys.insert(tablename, hashamap);
			
			tmpMap.put("sbbh", sbbh);
			addDynamicTables(tmpMap, parameters, tmpMap.get("sbztdm").toString());
			
			
			System.out.println("插入数量" + count);

		}
		if("9003".equals(type)){
			Map<String, Object> ss_map = new HashMap<String, Object>();
			Date date = new Date();
			ss_map.put("sbbh", sbbh);
			ss_map.put("sjkssj", date);
			ss_map.put("xtdqsj", date);
			ss_map.put("sbztdm", "40");
			Sys.insert("interf_sbztjk", ss_map);
			
			//修改其他的状态
			parameters.set("sbbh",sbbh);
			updateSsTable(parameters, bundle);
			//发送消息
			addDynamicTables(ss_map, parameters, "40");
			
			
		}
		if("9004".equals(type)){
			JSONObject jsonObject = JSONObject.fromObject(jsonContent);
			Map<String, Object> tmpMap = (Map<String, Object>) JSONObject.toBean(jsonObject, HashMap.class);
			Parameters p = new Parameters();
			p.set("sbbh", sbbh);
			String cxmc = tmpMap.get("cxmc").toString();
			String cxbz = tmpMap.get("cxbz").toString();
			//TODO
			p.set("pass_cxmc", cxmc + cxbz);
			p.set("pl_parcount", tmpMap.get("bgsl"));
			p.set("uuid", uuid);
			bg_5004(p, bundle);
		}
		logger.info("type=" + type+";;;uuid="+uuid+";;结束");
	}
	
	public static Date string2Date(String timeStr){
	    if(!timeStr.contains(":")){
	    	timeStr = timeStr + " 00:00:00";
	    }
	    String format ="";
	    if(timeStr.contains("-")){
	    	format = "yyyy-MM-dd HH:mm:ss";
	    }
	    if(timeStr.contains("/")){
	    	format = "yyyy/MM/dd HH:mm:ss";
	    }
	    SimpleDateFormat formatter=new SimpleDateFormat(format);  
	    try {
			return formatter.parse(timeStr);
		} catch (ParseException e) {
			logger.info("时间转换出现异常;;;"+timeStr);
			logger.error(e.getMessage());
			return null;
		} 
	}
	
	public static void main(String[] args) {
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("sbztdm", "30");
		JSONObject json = JSONObject.fromObject(map);
		System.err.println(json);
	}
}
