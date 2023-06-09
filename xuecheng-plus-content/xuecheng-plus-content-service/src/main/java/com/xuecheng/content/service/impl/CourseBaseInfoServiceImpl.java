package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author kj
 * @date 2023/3/13
 * @apiNote
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
   @Autowired
   CourseBaseMapper courseBaseMapper;

   @Autowired
   CourseMarketMapper courseMarketMapper;

   @Autowired
   CourseCategoryMapper courseCategoryMapper;

   @Autowired
   CourseMarketServiceImpl courseMarketService;

   @Autowired
   CourseTeacherMapper courseTeacherMapper;

   @Autowired
   TeachplanMapper teachplanMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //构建查询条件，根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getAuditStatus,queryCourseParamsDto.getPublishStatus());

        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //对参数进行合法性的校验
        //合法性校验
        // if (org.apache.commons.lang.StringUtils.isBlank(dto.getName())) {
        //     throw new XueChengPlusException("课程名称为空");
        // }
        // if (org.apache.commons.lang.StringUtils.isBlank(dto.getMt())) {
        //     throw new XueChengPlusException("课程分类为空");
        // }
        // if (org.apache.commons.lang.StringUtils.isBlank(dto.getSt())) {
        //     throw new XueChengPlusException("课程分类为空");
        // }
        // if (org.apache.commons.lang.StringUtils.isBlank(dto.getGrade())) {
        //     throw new XueChengPlusException("课程等级为空");
        // }
        // if (org.apache.commons.lang.StringUtils.isBlank(dto.getTeachmode())) {
        //     throw new XueChengPlusException("教育模式为空");
        // }
        // if (org.apache.commons.lang.StringUtils.isBlank(dto.getUsers())) {
        //     throw new XueChengPlusException("适应人群为空");
        // }
        // if (org.apache.commons.lang.StringUtils.isBlank(dto.getCharge())) {
        //     throw new XueChengPlusException("收费规则为空");
        // }

        //对数据进行封装，调用mapper进行数据持久化
        CourseBase courseBase = new CourseBase();
        //将传入dto的数据设置到 courseBase对象
//        courseBase.setName(dto.getName());
//        courseBase.setMt(dto.getMt());
//        courseBase.setSt(dto.getSt());
        //将dto中和courseBase属性名一样的属性值拷贝到courseBase
        BeanUtils.copyProperties(dto,courseBase);
        //设置机构id
        courseBase.setCompanyId(companyId);
        //创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        courseBase.setStatus("203001");
        //课程基本表插入一条记录
        int insert = courseBaseMapper.insert(courseBase);
        //获取课程id
        Long courseId = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        //将dto中和courseMarket属性名一样的属性值拷贝到courseMarket
        BeanUtils.copyProperties(dto,courseMarket);
        courseMarket.setId(courseId);
        //校验如果课程为收费，价格必须输入
        // String charge = dto.getCharge();
        // if(charge.equals("201001")){//收费
        //     if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue()<=0){
        //         throw new RuntimeException("课程为收费但是价格为空");
        //     }
        // }

        //向课程营销表插入一条记录
        int insert1 = courseMarketMapper.insert(courseMarket);

        if(insert<=0|| insert1<=0){
            log.error("创建课程过程中出错:{}",dto);
            throw new RuntimeException("创建课程过程中出错");
        }

        //组装要返回的结果
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

    /**
     * 根据课程id查询课程的基本和营销信息
     * @param courseId 课程id
     * @return 课程的信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){

        //基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);

        //根据课程分类的id查询分类的名称
        String mt = courseBase.getMt();
        String st = courseBase.getSt();

        CourseCategory mtCategory = courseCategoryMapper.selectById(mt);
        CourseCategory stCategory = courseCategoryMapper.selectById(st);
        if(mtCategory!=null){
            //分类名称
            String mtName = mtCategory.getName();
            courseBaseInfoDto.setMtName(mtName);
        }
        if(stCategory!=null){
            //分类名称
            String stName = stCategory.getName();
            courseBaseInfoDto.setStName(stName);
        }


        return courseBaseInfoDto;

    }

    @Override
    public void deleteCourseBaseInfo(Long courseId) {
        LambdaQueryWrapper<CourseBase> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBase::getId,courseId);
        courseBaseMapper.delete(queryWrapper);

        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper=new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId,courseId);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);

        LambdaQueryWrapper<CourseTeacher> teacherLambdaQueryWrapper=new LambdaQueryWrapper<>();
        teacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId,courseId);
        courseTeacherMapper.delete(teacherLambdaQueryWrapper);

        LambdaQueryWrapper<CourseMarket> marketLambdaQueryWrapper=new LambdaQueryWrapper<>();
        marketLambdaQueryWrapper.eq(CourseMarket::getId,courseId);
        courseMarketMapper.delete(marketLambdaQueryWrapper);
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {

        //校验
        //课程id
        Long id = dto.getId();
        CourseBase courseBase =
                courseBaseMapper.selectById(id);
        if(courseBase==null){
            XueChengPlusException.cast("课程不存在");
        }

        //校验本机构只能修改本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            log.info("courseBase.getCompanyId(){}",courseBase.getCompanyId());
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }

        //封装基本信息的数据
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        //更新课程基本信息
        int i = courseBaseMapper.updateById(courseBase);

        //封装营销信息的数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);

        //校验如果课程为收费，必须输入价格且大于0
//        String charge = courseMarket.getCharge();
//        if(charge.equals("201001")){
//            if(courseMarket.getPrice()==null || courseMarket.getPrice().floatValue()<=0){
////                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
//                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
//
//            }
//        }

        //请求数据库
        //对营销表有则更新，没有则添加
//        boolean b = courseMarketService.saveOrUpdate(courseMarket);

        saveCourseMarket(courseMarket);
        //查询课程信息
        CourseBaseInfoDto courseBaseInfo = this.getCourseBaseInfo(id);
        return courseBaseInfo;
    }

    //抽取对营销的保存
    private int saveCourseMarket(CourseMarket courseMarket){


        String charge = courseMarket.getCharge();
        if(org.apache.commons.lang.StringUtils.isBlank(charge)){
            XueChengPlusException.cast("收费规则没有选择");
        }
        if(charge.equals("201001")){
            if(courseMarket.getPrice()==null || courseMarket.getPrice().floatValue()<=0){
//                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");

            }
        }

        //保存
        boolean b = courseMarketService.saveOrUpdate(courseMarket);

        return b?1:0;

    }



}
