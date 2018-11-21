package com.wis.mes.master.workcalendar.service.impl;

import static com.wis.basis.common.utils.GetPropertiesMessageUtils.getMessage;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wis.basis.common.utils.BaseExcelUtil;
import com.wis.basis.common.utils.ConstantUtils;
import com.wis.basis.common.utils.DateUtils;
import com.wis.basis.common.utils.LoadUtils;
import com.wis.basis.common.utils.SpiltUtils;
import com.wis.basis.excel.pojo.ExcelImportPojo;
import com.wis.basis.systemadmin.entity.ImportLog;
import com.wis.basis.systemadmin.service.ImportLogService;
import com.wis.core.framework.entity.DictEntry;
import com.wis.core.framework.service.DictEntryService;
import com.wis.core.framework.service.GlobalConfigurationService;
import com.wis.core.framework.service.exception.BusinessException;
import com.wis.core.service.impl.BaseServiceImpl;
import com.wis.mes.master.line.entity.TmLine;
import com.wis.mes.master.line.service.TmLineService;
import com.wis.mes.master.plant.entity.TmPlant;
import com.wis.mes.master.plant.service.TmPlantService;
import com.wis.mes.master.workcalendar.dao.TmWorktimeDao;
import com.wis.mes.master.workcalendar.entity.TmWorktime;
import com.wis.mes.master.workcalendar.entity.TmWorktimeRest;
import com.wis.mes.master.workcalendar.service.TmWorktimeRestService;
import com.wis.mes.master.workcalendar.service.TmWorktimeService;
import com.wis.mes.master.workshop.entity.TmWorkshop;
import com.wis.mes.master.workshop.service.TmWorkshopService;

@Service
public class TmWorktimeServiceImpl extends BaseServiceImpl<TmWorktime> implements TmWorktimeService {

	@Autowired
	public void setDao(TmWorktimeDao dao) {
		this.dao = dao;
	}

	@Autowired
	private ImportLogService importLogService;
	@Autowired
	private TmWorktimeRestService worktimeRestService;
	@Autowired
	private DictEntryService entryService;
	@Autowired
	private GlobalConfigurationService globalConfigurationService;
	@Autowired
	private TmPlantService plantService;
	@Autowired
	private TmWorkshopService workshopService;
	@Autowired
	private TmLineService lineService;
	SimpleDateFormat sDateFormat = new SimpleDateFormat(DateUtils.FORMAT_DATE_DEFAULT);
	SimpleDateFormat timeFormat = new SimpleDateFormat(DateUtils.FORMAT_TIME_HH_MM_SS);
	SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.FORMAT_DATETIME_DEFAULT);

	public String queryWorkTimeByParams(Integer tmPlantId, Integer tmWorkshopId, Integer tmLineId, String shiftno,
			String[] workDate) {
		return ((TmWorktimeDao) dao).queryWorkTimeByParams(tmPlantId, tmWorkshopId, tmLineId, shiftno, workDate);
	}

	public void deleteWorkTimeARestByIds(Integer[] workTimeIds) {
		String workTimeRestIds = worktimeRestService.queryRestIdsByWorkTimeId(workTimeIds);
		if (null != workTimeRestIds) {
			String[] workTimeRestId = workTimeRestIds.split(",");
			Integer[] deleteWorkTimeRestIds = new Integer[workTimeRestId.length];
			for (int i = 0; i < workTimeRestId.length; i++) {
				deleteWorkTimeRestIds[i] = Integer.valueOf(workTimeRestId[i]);
			}
			worktimeRestService.doDeleteById(deleteWorkTimeRestIds);
		}
		doDeleteById(workTimeIds);
	}

	public Map<String, Object> exportExcelData(HttpServletResponse response, List<TmWorktime> list, String filePath,
			String[] header) {
		List<Map<String, Object>> exportDataList = new ArrayList<Map<String, Object>>();
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String> enabled = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.TYPE_CODE_ENABLED)) {
			enabled.put(e.getCode(), e.getName());
		}
		Map<String, String> shiftNo = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.SHIFT_TYPE)) {
			shiftNo.put(e.getCode(), e.getName());
		}
		Map<String, String> weekValue = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.WEEK_VALUE)) {
			weekValue.put(e.getCode(), e.getName());
		}
		for (int i = 0; i < list.size(); i++) {
			TmWorktime worktime = list.get(i);
			Map<String, Object> map = new HashMap<String, Object>();
			String enabledValue = enabled.get(worktime.getEnabled());
			worktime.setEnabled(enabledValue);
			String shiftValue = shiftNo.get(worktime.getShiftno());
			worktime.setShiftno(shiftValue);
			String week = weekValue.get(worktime.getWeek());
			worktime.setWeek(week);
			map.put("工厂", worktime.getPlant().getNo() + "-" + worktime.getPlant().getNameCn());
			map.put("车间", worktime.getWorkshop().getNo() + "-" + worktime.getWorkshop().getNameCn());
			map.put("产线", worktime.getLine().getNo() + "-" + worktime.getLine().getNameCn());
			map.put("工作日期", worktime.getWorkDate());
			map.put("班次", worktime.getShiftno());
			map.put("星期", worktime.getWeek());
			map.put("开始时间", worktime.getStartTime());
			map.put("结束时间", worktime.getEndTime());
			map.put("计划上线数量", worktime.getPlanOnlineQty() == null ? "" : worktime.getPlanOnlineQty());
			map.put("计划下线数量", worktime.getPlanOfflineQty() == null ? "" : worktime.getPlanOfflineQty());
			map.put("参考JPH", worktime.getJphQty() == null ? "" : worktime.getJphQty());
			map.put("启用", worktime.getEnabled());

			exportDataList.add(map);
		}
		result = BaseExcelUtil.exportData(response, exportDataList, filePath, header);
		return result;
	}

	public Map<String, Object> exportExcelDataAll(HttpServletResponse response, List<TmWorktime> list,
			String parentHeadStr, String childHeadStr, String filePath) {
		String[] parentHead = parentHeadStr.split(",");
		String[] childHead = childHeadStr.split(",");
		List<Map<String, Object>> parentExportList = new ArrayList<Map<String, Object>>();
		Map<Integer, List<Map<String, Object>>> childExportMap = new HashMap<Integer, List<Map<String, Object>>>();
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String> enabled = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.TYPE_CODE_ENABLED)) {
			enabled.put(e.getCode(), e.getName());
		}
		Map<String, String> shiftNo = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.SHIFT_TYPE)) {
			shiftNo.put(e.getCode(), e.getName());
		}
		Map<String, String> weekValue = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.WEEK_VALUE)) {
			weekValue.put(e.getCode(), e.getName());
		}
		for (int i = 0; i < list.size(); i++) {
			TmWorktime worktime = list.get(i);
			Map<String, Object> map = new HashMap<String, Object>();
			String enabledValue = enabled.get(worktime.getEnabled());
			worktime.setEnabled(enabledValue);
			String shiftValue = shiftNo.get(worktime.getShiftno());
			worktime.setShiftno(shiftValue);
			String week = weekValue.get(worktime.getWeek());
			worktime.setWeek(week);
			map.put(parentHead[0], worktime.getId());
			map.put(parentHead[1], worktime.getPlant().getNo() + "-" + worktime.getPlant().getNameCn());
			map.put(parentHead[2], worktime.getWorkshop().getNo() + "-" + worktime.getWorkshop().getNameCn());
			map.put(parentHead[3], worktime.getLine().getNo() + "-" + worktime.getLine().getNameCn());
			map.put(parentHead[4], worktime.getWorkDate());
			map.put(parentHead[5], worktime.getShiftno());
			map.put(parentHead[6], worktime.getWeek());
			map.put(parentHead[7], worktime.getStartTime());
			map.put(parentHead[8], worktime.getEndTime());
			map.put(parentHead[9], (worktime.getPlanOnlineQty() == null ? "" : worktime.getPlanOnlineQty()));
			map.put(parentHead[10], (worktime.getPlanOfflineQty() == null ? "" : worktime.getPlanOfflineQty()));
			map.put(parentHead[11], (worktime.getJphQty() == null ? "" : worktime.getJphQty()));
			map.put(parentHead[12], worktime.getEnabled());
			TmWorktimeRest tmWorktimeRest = new TmWorktimeRest();
			tmWorktimeRest.setTmWorktimeId(worktime.getId());
			List<TmWorktimeRest> worktimeRests = worktimeRestService.findByEg(tmWorktimeRest);
			List<Map<String, Object>> childExportList = new ArrayList<Map<String, Object>>();
			for (int j = 0; j < worktimeRests.size(); j++) {
				Map<String, Object> childMap = new HashMap<String, Object>();
				TmWorktimeRest wRest = worktimeRests.get(j);
				childMap.put(childHead[0], wRest.getStartTime());
				childMap.put(childHead[1], wRest.getEndTime());
				childExportList.add(childMap);
			}
			childExportMap.put(worktime.getId(), childExportList);
			parentExportList.add(map);
		}
		result = BaseExcelUtil.exportDataAll(response, parentExportList, parentHead, childExportMap, childHead,
				filePath);
		return result;
	}

	/**
	 * @author zhf
	 *
	 * @date 2017/5/10
	 *
	 * @desc 工作日历excel模板导出
	 */
	@Override
	public Workbook getWorkbookTemp(final String downName, final String templatePath) {
		try {
			final Workbook wb = WorkbookFactory.create(new File(templatePath));
			return wb;
		} catch (final Exception e) {
			log.error("Error down assembleDefect template.", e);
			throw new BusinessException("ERROR_DOWNLOAD_FILE");
		}
	}

	@SuppressWarnings("unchecked")
	public String doImportExcelData(Workbook workbook, HttpServletRequest req) {
		// 覆盖或更新标识
		String repeatOrUpdateFlag = globalConfigurationService
				.getValueByKey(ConstantUtils.SYS_CONFIG_IMP_EXE_UPDATE_WHEN_REPEAT);
		// 回滚标识
		String isRollBack = globalConfigurationService.getValueByKey(ConstantUtils.EXCEL_IMPORT_IS_ALL_ROLLBACK);
		// 工厂Map
		Map<String, TmPlant> plantMap = new HashMap<String, TmPlant>();
		for (TmPlant e : plantService.findAll()) {
			plantMap.put(e.getNo() + "-" + e.getNameCn(), e);
		}
		// 车间Map
		Map<String, TmWorkshop> workShopMap = new HashMap<String, TmWorkshop>();
		for (final TmWorkshop e : workshopService.findAll()) {
			workShopMap.put(e.getTmPlantId() + e.getNo() + "-" + e.getNameCn(), e);
		}

		// 产线Map
		Map<String, TmLine> lineMap = new HashMap<String, TmLine>();
		for (TmLine e : lineService.findAll()) {
			lineMap.put(e.getTmPlantId() + "-" + e.getTmWorkshopId() + e.getNo() + "-" + e.getNameCn(), e);
		}
		// 已经存在的工作日历Map
		Map<String, TmWorktime> existWorkTimeMap = new HashMap<String, TmWorktime>();
		for (TmWorktime worktime : findAll()) {
			// 将工厂，车间，产线的id,工作日期，班次作为唯一校验
			existWorkTimeMap.put("" + worktime.getTmPlantId() + worktime.getTmWorkshopId() + worktime.getTmLineId()
					+ worktime.getWorkDate() + worktime.getShiftno(), worktime);
		}

		// 班次map
		Map<String, String> shiftMap = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.SHIFT_TYPE)) {
			shiftMap.put(e.getName(), e.getCode());
		}
		// 星期map
		Map<String, String> weekMap = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.WEEK_VALUE)) {
			weekMap.put(e.getName(), e.getCode());
		}
		// 是否启用map
		Map<String, String> enableMap = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.TYPE_CODE_ENABLED)) {
			enableMap.put(e.getName(), e.getCode());
		}

		// 需要新增的数据集合
		List<TmWorktime> addList = new ArrayList<TmWorktime>();
		// 需要修改的数据的Map
		Map<Integer, TmWorktime> updateMap = new HashMap<Integer, TmWorktime>();
		// 格式错误的信息
		List<String> errorInfos = new ArrayList<String>();

		// 读取第一章表格内容
		final Sheet sheet = workbook.getSheetAt(0);
		// "第"
		final String DI = getMessage(req, "DI");
		String value = null;
		Row row;
		int totalInt = 0;
		String eL = "/^[\\w-\\s]+$/";
		Pattern p = Pattern.compile(eL);
		Matcher m = null;
		// 循环输出表格中的内容
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			totalInt++;
			row = sheet.getRow(i); // 获得行
			int index = 0;
			TmWorktime entity = new TmWorktime();

			// ======= 工 厂 校 验 =======
			value = LoadUtils.getCell(row, index).trim();
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_PLANT_REQUIRED"));
				continue;
			}
			if (!plantMap.containsKey(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_PLANT_NOT_EXIST"));
				continue;
			}
			// 判断工厂是否已启用
			if (ConstantUtils.TYPE_CODE_ENABLED_OFF.equals(plantMap.get(value).getEnabled())) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_PLANT_UN_ENABLED"));
				continue;
			}
			entity.setTmPlantId(plantMap.get(value).getId());

			// ======= 车 间 校 验 =========
			index++;
			value = LoadUtils.getCell(row, index).trim();
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_WORKSHOP_REQUIRED"));
				continue;
			}
			// 判断该工厂下是否有该车间
			if (!workShopMap.containsKey(entity.getTmPlantId() + value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_WORKSHOP_NOT_EXIST"));
				continue;
			}
			// 判断车间是否已启用
			if (ConstantUtils.TYPE_CODE_ENABLED_OFF
					.equals(workShopMap.get(entity.getTmPlantId() + value).getEnabled())) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_WORKSHOP_UN_ENABLED"));
				continue;
			}
			entity.setTmWorkshopId(workShopMap.get(entity.getTmPlantId() + value).getId());

			// ======= 产 线 校 验 =======
			index++;
			value = LoadUtils.getCell(row, index).trim();
			// 是否为空
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_LINE_REQUIRED"));
				continue;
			}
			// 产线编号名称格式判断
			if (!lineMap.containsKey(entity.getTmPlantId() + "-" + entity.getTmWorkshopId() + value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_LINE_NOT_EXIST"));
				continue;
			}
			// 判断产线是否已启用
			if (ConstantUtils.TYPE_CODE_ENABLED_OFF
					.equals(lineMap.get(entity.getTmPlantId() + "-" + entity.getTmWorkshopId() + value).getEnabled())) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_LINE_UN_ENABLED"));
				continue;
			}
			entity.setTmLineId(lineMap.get(entity.getTmPlantId() + "-" + entity.getTmWorkshopId() + value).getId());
			// ============ 工 作 日 期 ==============
			index++;
			value = LoadUtils.getCell(row, index).trim();
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_WORK_DATE_REQUIRED"));
				continue;
			}
			if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_WORK_DATE"));
				continue;
			}
			try {
				entity.setWorkDate(sDateFormat.parse(value));
			} catch (ParseException e2) {
				e2.printStackTrace();
			}
			;
			// ============ 班 次 校 验 ==============
			index++;
			value = LoadUtils.getCell(row, index).trim();
			// 当班次有值时判断是否符合定义类型
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_SHIFT_NO_REQUIRED"));
				continue;
			}
			if (null == shiftMap.get(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_SHIFT_DEFINE_ERROR"));
				continue;
			}
			entity.setShiftno(shiftMap.get(value));

			// ========== 星 期 ==========
			index++;
			// 导入时会根据具体日期自动设置星期值，无需从Excel中读取
			String weekDayName = null;
			try {
				weekDayName = DateUtils.getWeekDayNameByDateStr(sDateFormat.format(entity.getWorkDate()),
						DateUtils.FORMAT_DATE_DEFAULT);
				entity.setWeek(weekDayName);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			// ============ 开 始 时 间 ==============
			eL = "^(?:[01]\\d|2[0-3])(?::[0-5]\\d){1,2}$";
			index++;
			value = LoadUtils.getCell(row, index).trim();
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_START_TIME_REQUIRED"));
				continue;
			}
			p = Pattern.compile(eL);
			m = p.matcher(value);
			if (!m.matches()) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_START_TIME_ERROR"));
				continue;
			}
			// 功能暂时不用，注释
			// try {
			// entity.setStartTime(timeFormat.parse(value));
			// } catch (ParseException e1) {
			// e1.printStackTrace();
			// }
			// ========== 结 束 时 间 ============
			index++;
			value = LoadUtils.getCell(row, index).trim();
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_END_TIME_REQUIRED"));
				continue;
			}
			m = p.matcher(value);
			if (!m.matches()) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_END_TIME_ERROR"));
				continue;
			}
			//// 功能暂时不用，注释
			// try {
			// entity.setEndTime(timeFormat.parse(value));
			// } catch (ParseException e1) {
			// e1.printStackTrace();
			// }
			// ========== 计 划 上 线 数 ============
			index++;
			value = LoadUtils.getCell(row, index).trim();
			eL = "^\\d+$";
			p = Pattern.compile(eL);
			if (StringUtils.isNotBlank(value)) {
				m = p.matcher(value);
				if (!m.matches()) {
					errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_PLAN_ONLINE_QTY_ERROR"));
					continue;
				}
				entity.setPlanOnlineQty(Integer.parseInt(value));
			}
			// ========== 计 划 下 线 数 ============
			index++;
			value = LoadUtils.getCell(row, index).trim();
			if (StringUtils.isNotBlank(value)) {
				m = p.matcher(value);
				if (!m.matches()) {
					errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_PLAN_OFFLINE_QTY_ERROR"));
					continue;
				}
				entity.setPlanOfflineQty(Integer.parseInt(value));
			}
			// ========== 参考jph ============
			index++;
			String jph = LoadUtils.getCell(row, index).trim();
			entity.setJphQty(jph);

			// ========== 是 否 启 用 ============
			index++;
			value = LoadUtils.getCell(row, index).trim();
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_ENABLED_REQUIERED"));
				continue;
			}
			if (null == enableMap.get(value)) {
				errorInfos.add(DI + (i + 1) + getMessage(req, "WORK_TIME_ENABLED_VALUE_ERROR"));
				continue;
			}
			entity.setEnabled(enableMap.get(value));

			String checkKey = "" + entity.getTmPlantId() + entity.getTmWorkshopId() + entity.getTmLineId()
					+ sDateFormat.format(entity.getWorkDate()) + entity.getShiftno();
			// 新增还是更新，放入各自集合中
			if (existWorkTimeMap.containsKey(checkKey)) {
				updateMap.put(i + 1, entity);
			} else {
				addList.add(entity);
			}
		}
		// 保存操作
		List<TmWorktime> updateList = needUpdateEntitys(updateMap);
		StringBuffer addCount = new StringBuffer();
		StringBuffer updateCount = new StringBuffer();
		try {
			batchImport(addList, ConstantUtils.IMPORT_BATCH_COUNT, ConstantUtils.OPERATION_INSERT, addCount);
			// 修改数据
			if ("true".equals(repeatOrUpdateFlag)) {
				batchImport(updateList, ConstantUtils.IMPORT_BATCH_COUNT, null, updateCount);
			}
		} catch (Exception e) {
			if ("true".equals(isRollBack)) {
				throw new BusinessException("IMPORT_DATA_VALID_ERROR_INFO",
						getMessage(req, "IMPORT_UNKNOWN_EXCEPTION"));
			} else {
				return getMessage(req, "IMPORT_UNKNOWN_EXCEPTION");
			}
		}

		// 导入日志错误信息，页面提醒
		Set<Integer> repeatLindexes = updateMap.keySet();
		Map<String, Object> logsAndMsgTip = BaseExcelUtil.getLogsAndMsgTip(repeatOrUpdateFlag, addCount, updateCount,
				totalInt, repeatLindexes, errorInfos, req, getMessage(req, "WORK_TIME_MANAGEMENT"));

		importLogService.doSaveBatch((List<ImportLog>) logsAndMsgTip.get("logs"));
		return (String) logsAndMsgTip.get("msgTip");

	}

	private void batchImport(List<TmWorktime> list, int num, String insert, StringBuffer countBuffer) {
		int successCount = 0;
		if (list.size() > 0) {
			List<List<TmWorktime>> parts = SpiltUtils.averageAssign(list, num);
			try {
				for (int i = 0; i < parts.size(); i++) {
					if ("insert".equals(insert)) {
						doSaveBatch(parts.get(i));
						successCount += parts.get(i).size();
					} else {
						doUpdateBatch(parts.get(i));
						successCount += parts.get(i).size();
					}
				}
				countBuffer.append(successCount);
			} catch (Exception e) {
				countBuffer.append(successCount);
				throw new RuntimeException();
			}
		} else {
			countBuffer.append(successCount);
		}
	}

	private List<TmWorktime> needUpdateEntitys(Map<Integer, TmWorktime> updateTmWorktimeMap) {
		List<TmWorktime> updateList = new ArrayList<TmWorktime>();
		for (Integer key : updateTmWorktimeMap.keySet()) {
			TmWorktime insert = updateTmWorktimeMap.get(key);
			TmWorktime worktime = new TmWorktime();
			worktime.setTmPlantId(insert.getTmPlantId());
			worktime.setTmWorkshopId(insert.getTmWorkshopId());
			worktime.setTmLineId(insert.getTmLineId());
			worktime.setWorkDate(insert.getWorkDate());
			worktime.setShiftno(insert.getShiftno());
			TmWorktime newData = findUniqueByEg(worktime);
			newData.setStartTime(insert.getStartTime());
			newData.setEndTime(insert.getEndTime());
			newData.setEnabled(insert.getEnabled());
			newData.setShiftno(insert.getShiftno());
			newData.setWorkDate(insert.getWorkDate());
			newData.setPlanOnlineQty(insert.getPlanOnlineQty());
			newData.setPlanOfflineQty(insert.getPlanOfflineQty());
			newData.setJphQty(insert.getJphQty());
			newData.setWeek(insert.getWeek());
			updateList.add(newData);
		}
		return updateList;
	}

	/**
	 * @desc 级联导入工作日历及休息时间
	 * @date 17/06/14
	 */
	@SuppressWarnings("unchecked")
	public String doImportExcelDataAll(Workbook workbook, ExcelImportPojo excelImportPojo, HttpServletRequest req) {

		Map<String, Object> resultMap = null;
		try {
			resultMap = BaseExcelUtil.readExcelDataAll(workbook, excelImportPojo);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		// 读取excel中的工作日历
		Map<Integer, Object> workTimeMap = (Map<Integer, Object>) resultMap.get("parentMap");
		// 读取excel中的工作日历休息时间
		Map<Integer, Object> workTimeRestMap = (Map<Integer, Object>) resultMap.get("childrenMap");

		// 覆盖或更新标识
		String repeatOrUpdateFlag = globalConfigurationService
				.getValueByKey(ConstantUtils.SYS_CONFIG_IMP_EXE_UPDATE_WHEN_REPEAT);
		// 回滚标识
		String isRollBack = globalConfigurationService.getValueByKey(ConstantUtils.EXCEL_IMPORT_IS_ALL_ROLLBACK);

		// 工厂Map
		Map<String, TmPlant> plantMap = new HashMap<String, TmPlant>();
		for (TmPlant e : plantService.findAll()) {
			plantMap.put(e.getNo() + "-" + e.getNameCn(), e);
		}
		// 车间Map
		Map<String, TmWorkshop> workShopMap = new HashMap<String, TmWorkshop>();
		for (final TmWorkshop e : workshopService.findAll()) {
			workShopMap.put(e.getTmPlantId() + "-" + e.getNo() + "-" + e.getNameCn(), e);
		}
		// 产线Map
		Map<String, TmLine> lineMap = new HashMap<String, TmLine>();
		for (TmLine e : lineService.findAll()) {
			lineMap.put(e.getTmPlantId() + "-" + e.getTmWorkshopId() + "-" + e.getNo() + "-" + e.getNameCn(), e);
		}
		// 错误信息集合
		List<String> errorInfos = new ArrayList<String>();

		// 已经存在的工作日历Map
		Map<String, TmWorktime> existWorkTimeMap = new HashMap<String, TmWorktime>();
		for (TmWorktime worktime : findAll()) {
			// 将工厂，车间，产线的id,工作日期，班次作为唯一校验
			existWorkTimeMap.put(worktime.getTmPlantId() + "-" + worktime.getTmWorkshopId() + "-"
					+ worktime.getTmLineId() + "-" + worktime.getWorkDate() + "-" + worktime.getShiftno(), worktime);
		}
		// 班次map
		Map<String, String> shiftMap = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.SHIFT_TYPE)) {
			shiftMap.put(e.getName(), e.getCode());
		}
		// 星期map
		Map<String, String> weekMap = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.WEEK_VALUE)) {
			weekMap.put(e.getName(), e.getCode());
		}
		// 是否启用map
		Map<String, String> enableMap = new HashMap<String, String>();
		for (DictEntry e : entryService.getEntryByTypeCode(ConstantUtils.TYPE_CODE_ENABLED)) {
			enableMap.put(e.getName(), e.getCode());
		}
		String DI = getMessage(req, "DI");
		String value = null;
		// 保存成功、更新成功，主表记录总条数的计数器
		int addCountInt = 0;
		int updateCountInt = 0;
		int totalInt = 0;
		// 重复行标记录器
		Set<Integer> repeatLindexes = new TreeSet<Integer>();
		for (Integer key : workTimeMap.keySet()) {
			totalInt++;
			TmWorktime tmWorktime = (TmWorktime) workTimeMap.get(key);
			// 需要保存的工作日历休息时间
			// ======================== 主 表 =====================

			// ----------- 工 厂 校 验 -------------
			// 是否为空
			value = tmWorktime.getPlant().getNameCn();
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_PLANT_REQUIRED"));
				continue;
			}
			// 判断工厂编号是否存在
			if (null == plantMap.get(value)) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_PLANT_NOT_EXIST"));
				continue;
			}
			// 判断工厂是否已启用
			if (ConstantUtils.TYPE_CODE_ENABLED_OFF.equals(plantMap.get(value).getEnabled())) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_PLANT_UN_ENABLED"));
				continue;
			}
			tmWorktime.setTmPlantId(plantMap.get(value).getId());

			// --------------- 车 间 校 验 ------------
			value = tmWorktime.getWorkshop().getNameCn();
			// 是否为空
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_WORKSHOP_REQUIRED"));
				continue;
			}
			// 判断车间是否存在
			if (workShopMap.get(tmWorktime.getTmPlantId() + "-" + value) == null) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_WORKSHOP_NOT_EXIST"));
				continue;
			}
			// 判断车间是否已启用
			if (ConstantUtils.TYPE_CODE_ENABLED_OFF
					.equals(workShopMap.get(tmWorktime.getTmPlantId() + "-" + value).getEnabled())) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_WORKSHOP_UN_ENABLED"));
				continue;
			}

			tmWorktime.setTmWorkshopId(workShopMap.get(tmWorktime.getTmPlantId() + "-" + value).getId());

			// -------------- 产 线 校 验 ------------
			value = tmWorktime.getLine().getNameCn();
			// 是否为空
			if (StringUtils.isBlank(value)) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_LINE_REQUIRED"));
				continue;
			}
			// 判断产线是否存在
			if (lineMap.get(tmWorktime.getTmPlantId() + "-" + tmWorktime.getTmWorkshopId() + "-" + value) == null) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_LINE_NOT_EXIST"));
				continue;
			}
			// 判断产线是否已启用
			if (ConstantUtils.TYPE_CODE_ENABLED_OFF.equals(lineMap
					.get(tmWorktime.getTmPlantId() + "-" + tmWorktime.getTmWorkshopId() + "-" + value).getEnabled())) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_LINE_UN_ENABLED"));
				continue;
			}
			tmWorktime.setTmLineId(
					lineMap.get(tmWorktime.getTmPlantId() + "-" + tmWorktime.getTmWorkshopId() + "-" + value).getId());

			// ----------工作日期---------
			Date workDate = tmWorktime.getWorkDate();
			if (null != workDate) {
				try {
					sDateFormat.parse(sDateFormat.format(workDate));
				} catch (ParseException e) {
					errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_WORK_DATE"));
					continue;
				}
			} else {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_WORK_DATE_REQUIRED"));
				continue;
			}

			// ---------- 班 次 校 验 --------
			String shiftNo = tmWorktime.getShiftno();
			// 当班次有值时判断是否符合定义类型
			if (StringUtils.isNotBlank(shiftNo)) {
				if (null == shiftMap.get(shiftNo)) {
					errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_SHIFT_DEFINE_ERROR"));
					continue;
				} else {
					tmWorktime.setShiftno(shiftMap.get(shiftNo));
				}
			} else {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_SHIFT_REQUIRED"));
				continue;
			}

			// ----------- 星 期 校 验 ------------

			// 导入时会根据具体日期自动设置星期值，无需校验
			String weekDayName = null;
			try {
				weekDayName = DateUtils.getWeekDayNameByDateStr(sDateFormat.format(tmWorktime.getWorkDate()),
						DateUtils.FORMAT_DATE_DEFAULT);
				tmWorktime.setWeek(weekDayName);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			// ---------- 开 始 时 间 --------
			// Date startTime = tmWorktime.getStartTime();
			// String eL = "^(?:[01]\\d|2[0-3])(?::[0-5]\\d){1,2}$";
			// Pattern p = Pattern.compile(eL);
			// Matcher m = null;
			// if (null != startTime) {
			// m = p.matcher(timeFormat.format(startTime));
			// if (!m.matches()) {
			// errorInfos.add(DI + (key + 1) + getMessage(req,
			// "WORK_TIME_START_TIME_ERROR"));
			// continue;
			// }
			// } else {
			// errorInfos.add(DI + (key + 1) + getMessage(req,
			// "WORK_TIME_START_TIME_REQUIRED"));
			// continue;
			// }
			//
			// // ---------- 结 束 时 间 --------
			// Date endTime = tmWorktime.getEndTime();
			// if (null != endTime) {
			// m = p.matcher(timeFormat.format(endTime));
			// if (!m.matches()) {
			// errorInfos.add(DI + (key + 1) + getMessage(req,
			// "WORK_TIME_END_TIME_ERROR"));
			// continue;
			// }
			// } else {
			// errorInfos.add(DI + (key + 1) + getMessage(req,
			// "WORK_TIME_END_TIME_REQUIRED"));
			// continue;
			// }

			// // ------------ 计 划 上 线 数 量 ----------
			// Integer planOnlineQty = tmWorktime.getPlanOnlineQty();
			// eL = "^\\d+$";
			// p = Pattern.compile(eL);
			// if (null != planOnlineQty) {
			// m = p.matcher(String.valueOf(planOnlineQty));
			// if (!m.matches()) {
			// errorInfos.add(DI + (key + 1) + getMessage(req,
			// "WORK_TIME_PLAN_ONLINE_QTY_ERROR"));
			// continue;
			// }
			// }
			//
			// // ------------ 计 划 下 线 数 量 ----------
			// Integer planOfflineQty = tmWorktime.getPlanOfflineQty();
			// if (null != planOfflineQty) {
			// m = p.matcher(String.valueOf(planOfflineQty));
			// if (!m.matches()) {
			// errorInfos.add(DI + (key + 1) + getMessage(req,
			// "WORK_TIME_PLAN_OFFLINE_QTY_ERROR"));
			// continue;
			// }
			// }
			// ------------ 参考JPH ----------
			// 参考JPH无需校验

			// ------------ 是 否 启 用 ----------
			String enable = tmWorktime.getEnabled();
			if (StringUtils.isBlank(enable)) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_ENABLED_REQUIERED"));
				continue;
			}
			if (null == enableMap.get(enable)) {
				errorInfos.add(DI + (key + 1) + getMessage(req, "WORK_TIME_ENABLED_VALUE_ERROR"));
				continue;
			}
			tmWorktime.setEnabled(enableMap.get(enable));

			// 校验重复数据唯一性
			String checkKey = tmWorktime.getTmPlantId() + "-" + tmWorktime.getTmWorkshopId() + "-"
					+ tmWorktime.getTmLineId() + "-" + sDateFormat.format(tmWorktime.getWorkDate()) + "-"
					+ tmWorktime.getShiftno();

			TmWorktime existWorkTime = existWorkTimeMap.get(checkKey);
			// repeatOrUpdateFlag为true表示更新
			if ("true".equals(repeatOrUpdateFlag)) {
				String childrenErrMsg = null;
				try {
					String operation = null;
					if (null == existWorkTime) {
						operation = "insert";
						childrenErrMsg = saveWorkTimeRest(operation, tmWorktime, key, workTimeRestMap, req);
					} else {
						operation = "update";
						childrenErrMsg = saveWorkTimeRest(operation, tmWorktime, key, workTimeRestMap, req);
					}
					if (childrenErrMsg != null) {
						errorInfos.add(childrenErrMsg);
						continue;
					}
					if ("insert".equals(operation)) {
						addCountInt++;
					} else {
						updateCountInt++;
					}

				} catch (Exception e) {
					e.printStackTrace();
					if ("true".equals(isRollBack)) {
						throw new BusinessException("IMPORT_UNKNOWN_EXCEPTION");
					} else {
						return getMessage(req, "IMPORT_UNKNOWN_EXCEPTION");
					}
				}
			} else {
				if (existWorkTime != null) {
					// 记录重复数据的行号
					repeatLindexes.add(key + 1);
				}
			}
		}

		if (resultMap.get("blankLineInfo") != null) {
			errorInfos.add((String) resultMap.get("blankLineInfo"));
		}
		StringBuffer addCount = new StringBuffer().append(addCountInt);
		StringBuffer updateCount = new StringBuffer().append(updateCountInt);
		Map<String, Object> logsAndMsgTip = BaseExcelUtil.getLogsAndMsgTip(repeatOrUpdateFlag, addCount, updateCount,
				totalInt, repeatLindexes, errorInfos, req, getMessage(req, "WORK_TIME_MANAGEMENT"));

		importLogService.doSaveBatch((List<ImportLog>) logsAndMsgTip.get("logs"));
		return (String) logsAndMsgTip.get("msgTip");
	}

	private String saveWorkTimeRest(String operation, TmWorktime tmWorktime, Integer key,
			Map<Integer, Object> workTimeRestMap, HttpServletRequest request) {

		List<TmWorktimeRest> needSaveWorkTimeRest = new ArrayList<TmWorktimeRest>();
		@SuppressWarnings("unchecked")
		List<TmWorktimeRest> tmWorktimeRestList = (List<TmWorktimeRest>) workTimeRestMap.get(key);
		// Date startTime = tmWorktime.getStartTime();
		// Date endTime = tmWorktime.getEndTime();
		// Integer workTimeStart =
		// Integer.parseInt(timeFormat.format(startTime).replaceAll(":", ""));
		// Integer workTimeEnd =
		// Integer.parseInt(timeFormat.format(endTime).replaceAll(":", ""));
		@SuppressWarnings("unused")
		String result = null;

		if (null != tmWorktimeRestList && tmWorktimeRestList.size() > 0) {
			// 子表数据不为空时
			for (int i = 0; i < tmWorktimeRestList.size(); i++) {
				TmWorktimeRest tmWorktimeRest = tmWorktimeRestList.get(i);
				@SuppressWarnings("unused")
				Integer startRestTime = Integer
						.parseInt(timeFormat.format(tmWorktimeRest.getStartTime()).replaceAll(":", ""));
				@SuppressWarnings("unused")
				Integer endRestTime = Integer
						.parseInt(timeFormat.format(tmWorktimeRest.getEndTime()).replaceAll(":", ""));
				// if (null != tmWorktimeRest.getStartTime()) {
				// if (startRestTime < workTimeStart || startRestTime >
				// workTimeEnd) {
				// result = "第" + (key + 2 + i) + getMessage(request,
				// "WORK_TIME_START_REST_ERROR");
				// return result;
				// }
				// }
				// if (null != tmWorktimeRest.getEndTime()) {
				// if (endRestTime < workTimeStart || endRestTime > workTimeEnd)
				// {
				// result = "第" + (key + 2 + i) + getMessage(request,
				// "WORK_TIME_END_REST_ERROR");
				// return result;
				// }
				// }
				needSaveWorkTimeRest.add(tmWorktimeRest);
			}
		}

		TmWorktime saveOrUpdateWorkTime = null;
		if ("insert".equals(operation)) {
			saveOrUpdateWorkTime = doSave(tmWorktime);
		} else {
			TmWorktime eg = new TmWorktime();
			eg.setTmPlantId(tmWorktime.getTmPlantId());
			eg.setTmWorkshopId(tmWorktime.getTmWorkshopId());
			eg.setTmLineId(tmWorktime.getTmLineId());
			eg.setWorkDate(tmWorktime.getWorkDate());
			eg.setShiftno(tmWorktime.getShiftno());
			TmWorktime uniqueEntity = findUniqueByEg(eg);
			tmWorktime.setId(uniqueEntity.getId());
			tmWorktime.setTmWorkscheduleId(uniqueEntity.getTmWorkscheduleId());
			saveOrUpdateWorkTime = doUpdate(tmWorktime);
		}

		for (TmWorktimeRest rest : needSaveWorkTimeRest) {
			rest.setTmWorktimeId(saveOrUpdateWorkTime.getId());
		}

		// 当更新主表数据时，才需要删除以前子表数据并添加新的子表数据
		if ("update".equals(operation)) {
			TmWorktimeRest needDeleteWorktimeRest = new TmWorktimeRest();
			needDeleteWorktimeRest.setTmWorktimeId(saveOrUpdateWorkTime.getId());
			List<TmWorktimeRest> worktimeRests = worktimeRestService.findByEg(needDeleteWorktimeRest);
			if (worktimeRests.size() > 0) {
				Integer[] deleteWorktimeRestIds = new Integer[worktimeRests.size()];
				for (int i = 0; i < worktimeRests.size(); i++) {
					TmWorktimeRest delWorktimeRest = worktimeRests.get(i);
					deleteWorktimeRestIds[i] = delWorktimeRest.getId();
				}
				worktimeRestService.doDeleteById(deleteWorktimeRestIds);
			}
		}
		worktimeRestService.doSaveBatch(needSaveWorkTimeRest);
		return null;
	}

	@Override
	public Map<String, Object> getEveryShiftTime(TmWorktime worktime, Integer workTimeId, String enabledNo) {
		Map<String, Object> map = new HashMap<String, Object>();
		String shiftno = worktime.getShiftno();
		worktime.setShiftno(null);
		// 先判断同样的班次是否已存在
		worktime.setEnabled(ConstantUtils.TYPE_CODE_ENABLED_ON);
		List<TmWorktime> workTimeList = findByEg(worktime);
		boolean existed = true;
		if (null != workTimeList && workTimeList.size() > 0 && ConstantUtils.TYPE_CODE_ENABLED_ON.equals(enabledNo)) {
			if (null == workTimeId) {
				existed = false;
			} 
			for (TmWorktime bean : workTimeList) {
				if (bean.getShiftno().equals(shiftno) && !bean.getId().equals(workTimeId)) {
					existed = false;
				}
				if (ConstantUtils.SHIFT_MORNING.equals(bean.getShiftno())) {
					map.put("morning", new Object[] { bean.getStartTime(), bean.getEndTime() });
				}else if (ConstantUtils.SHIFT_NOON.equals(bean.getShiftno())) {
					map.put("noon", new Object[] { bean.getStartTime(), bean.getEndTime() });
				}else if (ConstantUtils.SHIFT_NIGHT.equals(bean.getShiftno())) {
					map.put("night", new Object[] { bean.getStartTime(), bean.getEndTime() });
				}
			}
		}
		map.put("existed", existed);
		return map;
	}

	@Override
	public Integer getWorkTime(Map<String, Object> map) {
		return ((TmWorktimeDao) dao).getWorkTime(map);
	}

	@Override
	public TmWorktime thisWorkTime(Integer tmLineId) {
		return ((TmWorktimeDao) dao).thisWorkTime(tmLineId);
	}

	@Override
	public String breathingSpace(Integer tmWorktimeId, Integer rownum) {
		return ((TmWorktimeDao) dao).breathingSpace(tmWorktimeId, rownum);
	}

	@Override
	public Map<String, Object> sendPlcWorkingCalendar() {
		String lineNo = globalConfigurationService.getValueByKey(ConstantUtils.R5_LINE_NO);
		TmLine line = lineService.findUniqueByEg(TmLine.createLineNo(lineNo));
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != line) {
			TmWorktime workTime = thisWorkTime(line.getId());
			if (null != workTime) {
				map.put("currentShiftno", workTime.getShiftno());
				String startTime = workTime.getStartTime().split(" ")[1].replace(":", "");
				String endTime = workTime.getEndTime().split(" ")[1].replace(":", "");
				String breathingSpace = breathingSpace(workTime.getId(), ConstantUtils.REST_TEMPLATE_ROWNUM);
				map.put("Rest_Calendar", checkCreathingSpace(breathingSpace,40));// 休息时间
				map.put("Produce_Calendar", startTime + endTime);// 工作时间
				map.put("Dauly_Plan", workTime.getPlanOnlineQty());// 日计划
			}
		}
		return map;
	}

	@Override
	public Map<String, String> sendFinPlcWorkingCalendar() {
		String lineNo = globalConfigurationService.getValueByKey(ConstantUtils.XK_LINE_NO);
		TmLine line = lineService.findUniqueByEg(TmLine.createLineNo(lineNo));
		Map<String, String> map = new HashMap<String, String>();
		String rest = "";
		if (line != null) {
			TmWorktime workTime = getNextFlight(ConstantUtils.SHIFT_MORNING, line.getId());
			if (workTime != null) {
				 String daySet = workTime.getStartTime().replace(":", "") + workTime.getEndTime().replace(":", "");
				 String time = breathingSpace(workTime.getId(), ConstantUtils.REST_TEMPLATE_ROWNUM);
				 rest += checkCreathingSpace(time,40);
				 map.put("daySet", daySet);
			}
			workTime = getNextFlight(ConstantUtils.SHIFT_NIGHT, line.getId());
			if (workTime != null) {
				String netSet = workTime.getStartTime().replace(":", "") + workTime.getEndTime().replace(":", "");
				String time = breathingSpace(workTime.getId(), ConstantUtils.REST_TEMPLATE_ROWNUM);
				rest += checkCreathingSpace(time,40);
				map.put("netSet", netSet);
			}
			map.put("rest", rest);
		}
		return map;
	}
	
	
	@Override
	public Map<String, Object> sendBjPlcWorkingCalendar() {
		String lineNo = globalConfigurationService.getValueByKey(ConstantUtils.MP_LINE_NO);
		TmLine line = lineService.findUniqueByEg(TmLine.createLineNo(lineNo));
		Map<String, Object> map = new HashMap<String, Object>();
		String rest = "";
		String daySet = "",netSet="",middleSet="";
		if (line != null) {
			TmWorktime workTime = getNextFlight(ConstantUtils.SHIFT_MORNING, line.getId());
			if (workTime != null) {
				daySet = workTime.getStartTime().replace(":", "") + workTime.getEndTime().replace(":", "");
				String time = breathingSpace(workTime.getId(), ConstantUtils.REST_TEMPLATE_ROWNUM);
				rest += checkCreathingSpace(time,40);
			}
			map.put("daySet", StringUtils.isNotBlank(daySet)?daySet:"00000000");
			workTime = getNextFlight(ConstantUtils.SHIFT_NIGHT, line.getId());
			if (workTime != null) {
				netSet = workTime.getStartTime().replace(":", "") + workTime.getEndTime().replace(":", "");
				String time = breathingSpace(workTime.getId(), ConstantUtils.REST_TEMPLATE_ROWNUM);
				rest += checkCreathingSpace(time,40);
			}
			map.put("netSet", StringUtils.isNotBlank(netSet)?netSet:"00000000");
			workTime = getNextFlight(ConstantUtils.SHIFT_NOON, line.getId());
			if (workTime != null) {
				middleSet = workTime.getStartTime().replace(":", "") + workTime.getEndTime().replace(":", "");
				String time = breathingSpace(workTime.getId(), ConstantUtils.REST_TEMPLATE_ROWNUM);
				rest += checkCreathingSpace(time,40);
			}
			map.put("middleSet", StringUtils.isNotBlank(middleSet)?middleSet:"00000000");
			map.put("rest", rest);
		}
		return map;
	}
	
	@Override
	public void checkWorkTimeMain() {
		List<TmLine> lines = lineService.findAll();
		if(null != lines && lines.size() > 0) {
			for(TmLine line:lines) {
				TmWorktime workTime = thisWorkTime(line.getId());
				if(null != workTime){
					String shiftno = ConstantUtils.SHIFT_MORNING.equals(workTime.getShiftno())?ConstantUtils.SHIFT_NIGHT:ConstantUtils.SHIFT_MORNING;
					TmWorktime nextWorkTime = getNextFlight(shiftno,line.getId());//获取下个班次的工作日历
					if(null == nextWorkTime){
						TmWorktime lastDayWorkTime = lastDayWordkTime(shiftno, line.getId());
						if(null != lastDayWorkTime) {
							TmWorktime copyWorkTime = copyWorkTime(lastDayWorkTime,workTime);
							copyTimeRest(lastDayWorkTime.getId(),copyWorkTime.getId());
						}
					}
				}
			}
		}
	}
	
	//复制前一天的工作日历
	private TmWorktime copyWorkTime(TmWorktime lastDayWorkTime,TmWorktime thisWorkTime) {
		TmWorktime bean = new TmWorktime();
		bean.setEndTime(lastDayWorkTime.getEndTime());
		bean.setEnabled(lastDayWorkTime.getEnabled());
		bean.setPlanOfflineQty(lastDayWorkTime.getPlanOfflineQty());
		bean.setPlanOnlineQty(lastDayWorkTime.getPlanOnlineQty());
		bean.setTmPlantId(lastDayWorkTime.getTmPlantId());
		bean.setTmLineId(lastDayWorkTime.getTmLineId());
		bean.setShiftno(lastDayWorkTime.getShiftno());
		bean.setRemark(lastDayWorkTime.getRemark());
		bean.setTmClassManagerId(lastDayWorkTime.getTmClassManagerId());
		bean.setTmWorkscheduleId(lastDayWorkTime.getTmWorkscheduleId());
		bean.setWeek(lastDayWorkTime.getWeek());
		bean.setStartTime(lastDayWorkTime.getStartTime());
		if(ConstantUtils.SHIFT_MORNING.equals(thisWorkTime.getShiftno())) {
			bean.setWorkDate(thisWorkTime.getWorkDate());
		}else if(ConstantUtils.SHIFT_NIGHT.equals(thisWorkTime.getShiftno())){
			bean.setWorkDate(DateUtils.getSpecifiedDayAfter(thisWorkTime.getWorkDate()));
		}
		return doSave(bean);
	}
	
	//复制前一天休息时间
	private void copyTimeRest(Integer beforeWorkTimeId,Integer afterWorkTimeId) {
		TmWorktimeRest eg = new TmWorktimeRest();
		eg.setTmWorktimeId(beforeWorkTimeId);
		List<TmWorktimeRest> worktimeRestList = worktimeRestService.findByEg(eg);
		for(TmWorktimeRest timeRest:worktimeRestList) {
			TmWorktimeRest bean = new TmWorktimeRest();
			bean.setTmWorktimeId(afterWorkTimeId);
			bean.setEndTime(timeRest.getEndTime());
			bean.setStartTime(timeRest.getStartTime());
			bean.setRemark(timeRest.getRemark());
			worktimeRestService.doSave(bean);
		}
	}

	// 传给PLC的休息日期长度是40位，默认拼接40个0，截取前40位
	private String checkCreathingSpace(String timeOut,int zeroCount) {
		timeOut = (StringUtils.isNotEmpty(timeOut) ? timeOut : "");
		StringBuffer src = new StringBuffer(timeOut);
		for (int i = 0; i < zeroCount; i++) {
			src.append(0);
		}
		return src.toString().substring(0, 40);
	}

	@Override
	public TmWorktime getNextFlight(String shiftno, Integer tmLineId) {
		return ((TmWorktimeDao) dao).getNextFlight(shiftno, tmLineId);
	}

	@Override
	public TmWorktime getWorkTimeByDayAndShiftno(String queryDay, String lineNo, String shiftNo) {
		return ((TmWorktimeDao) dao).getWorkTimeByDayAndShiftno(queryDay, lineNo, shiftNo);
	}

	@Override
	public TmWorktime getDateWorkTime(Date queryDate, String lineNo) {
		return ((TmWorktimeDao) dao).getDateWorkTime(queryDate, lineNo);
	}

	@Override
	public TmWorktime lastDayWordkTime(String shiftno, Integer tmLineId) {
		return ((TmWorktimeDao) dao).lastDayWordkTime(shiftno, tmLineId);
	}


    /**
     * 计算某一个班次有多少分钟
     * @param tmWorktime
     * @return
     */
    @Override
    public Long calcMinutes(TmWorktime tmWorktime) {
        if(tmWorktime!=null){
            List<TmWorktimeRest> rests = worktimeRestService.findByWorkTimeId(tmWorktime.getId());
            long start =  getStartTime(tmWorktime).getTime(),end = getEndTime(tmWorktime).getTime();
            long allRestTime=0;

            if(rests!=null && !rests.isEmpty()) {
                String WorkDate = DateUtils.formatDate(tmWorktime.getWorkDate());
                String nxtWorkDate = DateUtils.formatDate(DateUtils.addDays(tmWorktime.getWorkDate(), 1));

                for (TmWorktimeRest rest : rests) {
                    long restStart = DateUtils.parse(WorkDate + " " + rest.getStartTime(), DateUtils.FORMAT_DATETIME_YYYY_MM_DD_HH_MM).getTime();
                    long restEnd = DateUtils.parse(WorkDate + " " + rest.getEndTime(), DateUtils.FORMAT_DATETIME_YYYY_MM_DD_HH_MM).getTime();
                    if (restEnd < restStart) {
                        restEnd = DateUtils.parse(nxtWorkDate + " " + rest.getEndTime(), DateUtils.FORMAT_DATETIME_YYYY_MM_DD_HH_MM).getTime();
                    }
                    allRestTime += (restEnd - restStart);
                }
            }
            return (end-start-allRestTime)/(1000*60);
        }
        return 0L;
    }

    @Override
    public Date getStartTime(TmWorktime tmWorktime){
        String WorkDate = DateUtils.formatDate(tmWorktime.getWorkDate());
        return DateUtils.parse(WorkDate+" "+tmWorktime.getStartTime(),DateUtils.FORMAT_DATETIME_YYYY_MM_DD_HH_MM);
    }

    @Override
    public Date getEndTime(TmWorktime tmWorktime){
        if(ConstantUtils.SHIFT_NIGHT.equals(tmWorktime.getShiftno())) {
            //晚班
            String WorkDate = DateUtils.formatDate(tmWorktime.getWorkDate());
            long start = getStartTime(tmWorktime).getTime();
            Date endTime = DateUtils.parse(WorkDate + " " + tmWorktime.getEndTime(), DateUtils.FORMAT_DATETIME_YYYY_MM_DD_HH_MM);
            //检查是否跨天
            if(start>endTime.getTime()){
                String nxtWorkDate = DateUtils.formatDate(DateUtils.addDays(tmWorktime.getWorkDate(), 1));
                return DateUtils.parse(nxtWorkDate + " " + tmWorktime.getEndTime(), DateUtils.FORMAT_DATETIME_YYYY_MM_DD_HH_MM);
            }else{
                return endTime;
            }
        }else{
            String WorkDate = DateUtils.formatDate(tmWorktime.getWorkDate());
            return DateUtils.parse(WorkDate+" "+tmWorktime.getEndTime(),DateUtils.FORMAT_DATETIME_YYYY_MM_DD_HH_MM);
        }
    }

	@Override
	public List<TmWorktime> todayWorkTime(Integer tmLineId,String shiftNo) {
		return ((TmWorktimeDao) dao).todayWorkTime(tmLineId,shiftNo);
	}
}
