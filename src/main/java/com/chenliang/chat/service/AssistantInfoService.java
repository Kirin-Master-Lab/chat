package com.chenliang.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chenliang.chat.entity.AssistantInfoEntity;
import com.chenliang.chat.mapper.AssistantInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AssistantInfoService {

    public static final String ASSISTANT_KIND_GENERAL = "GENERAL";
    public static final String ASSISTANT_KIND_DICT = "DICT";
    public static final String ASSISTANT_KIND_MAP = "MAP";

    private static final Pattern ASSISTANT_KIND_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_-]{0,63}$");

    @Resource
    private AssistantInfoMapper assistantInfoMapper;

    public List<AssistantInfoEntity> listAll() {
        return assistantInfoMapper.selectList(baseOrderWrapper());
    }

    public List<AssistantInfoEntity> listEnabled() {
        LambdaQueryWrapper<AssistantInfoEntity> wrapper = baseOrderWrapper();
        wrapper.eq(AssistantInfoEntity::getEnabled, 1);
        return assistantInfoMapper.selectList(wrapper);
    }

    public AssistantInfoEntity getEnabledByCode(String assistantCode) {
        String normalizedCode = normalizeAssistantCode(assistantCode);
        if (!StringUtils.hasText(normalizedCode)) {
            return null;
        }

        LambdaQueryWrapper<AssistantInfoEntity> wrapper = baseOrderWrapper();
        wrapper.eq(AssistantInfoEntity::getAssistantCode, normalizedCode)
                .eq(AssistantInfoEntity::getEnabled, 1)
                .last("limit 1");
        return assistantInfoMapper.selectOne(wrapper);
    }

    public AssistantInfoEntity getDefaultAssistant() {
        LambdaQueryWrapper<AssistantInfoEntity> wrapper = baseOrderWrapper();
        wrapper.eq(AssistantInfoEntity::getEnabled, 1)
                .eq(AssistantInfoEntity::getIsDefault, 1)
                .last("limit 1");
        AssistantInfoEntity assistant = assistantInfoMapper.selectOne(wrapper);
        if (assistant != null) {
            return assistant;
        }

        List<AssistantInfoEntity> enabledAssistants = listEnabled();
        return enabledAssistants.isEmpty() ? null : enabledAssistants.get(0);
    }

    public AssistantInfoEntity create(AssistantInfoEntity request) {
        AssistantInfoEntity entity = normalizeForCreate(request);
        validateDuplicateCode(entity.getAssistantCode(), null);

        if (entity.getIsDefault() == 1 || countEnabledAssistants() == 0) {
            entity.setEnabled(1);
            entity.setIsDefault(1);
        }

        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDeleted(0);
        assistantInfoMapper.insert(entity);

        if (entity.getIsDefault() == 1) {
            clearDefaultFlagForOthers(entity.getId());
        } else {
            ensureDefaultAssistant();
        }

        return assistantInfoMapper.selectById(entity.getId());
    }

    public AssistantInfoEntity update(AssistantInfoEntity request) {
        if (request == null || request.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少助手主键");
        }

        AssistantInfoEntity existing = assistantInfoMapper.selectById(request.getId());
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "助手不存在");
        }

        AssistantInfoEntity entity = normalizeForUpdate(request, existing);
        validateDuplicateCode(entity.getAssistantCode(), entity.getId());

        if (existing.getEnabled() == 1 && entity.getEnabled() == 0 && countEnabledAssistants() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少保留一个启用中的助手");
        }

        if (entity.getIsDefault() == 1) {
            entity.setEnabled(1);
        }

        entity.setCreateTime(existing.getCreateTime());
        entity.setDeleted(existing.getDeleted());
        entity.setUpdateTime(LocalDateTime.now());
        assistantInfoMapper.updateById(entity);

        if (entity.getIsDefault() == 1) {
            clearDefaultFlagForOthers(entity.getId());
        } else {
            ensureDefaultAssistant();
        }

        return assistantInfoMapper.selectById(entity.getId());
    }

    public void delete(Long id) {
        AssistantInfoEntity existing = assistantInfoMapper.selectById(id);
        if (existing == null) {
            return;
        }

        if (existing.getEnabled() == 1 && countEnabledAssistants() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少保留一个启用中的助手");
        }

        assistantInfoMapper.deleteById(id);
        ensureDefaultAssistant();
    }

    private void validateDuplicateCode(String assistantCode, Long currentId) {
        LambdaQueryWrapper<AssistantInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssistantInfoEntity::getAssistantCode, assistantCode);
        if (currentId != null) {
            wrapper.ne(AssistantInfoEntity::getId, currentId);
        }
        if (assistantInfoMapper.selectCount(wrapper) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "助手编码已存在");
        }
    }

    private AssistantInfoEntity normalizeForCreate(AssistantInfoEntity request) {
        AssistantInfoEntity entity = new AssistantInfoEntity();
        fillAssistantFields(entity, request);
        entity.setEnabled(normalizeFlag(request == null ? null : request.getEnabled(), 1));
        entity.setIsDefault(normalizeFlag(request == null ? null : request.getIsDefault(), 0));
        return entity;
    }

    private AssistantInfoEntity normalizeForUpdate(AssistantInfoEntity request, AssistantInfoEntity existing) {
        AssistantInfoEntity entity = new AssistantInfoEntity();
        entity.setId(existing.getId());
        fillAssistantFields(entity, request);
        entity.setEnabled(normalizeFlag(request.getEnabled(), existing.getEnabled()));
        entity.setIsDefault(normalizeFlag(request.getIsDefault(), existing.getIsDefault()));

        if (!existing.getAssistantCode().equals(entity.getAssistantCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "助手编码创建后不允许修改");
        }
        return entity;
    }

    private void fillAssistantFields(AssistantInfoEntity target, AssistantInfoEntity source) {
        if (source == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "助手信息不能为空");
        }

        String assistantCode = normalizeAssistantCode(source.getAssistantCode());
        if (!StringUtils.hasText(assistantCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "助手编码不能为空");
        }

        String assistantName = trimToNull(source.getAssistantName());
        if (!StringUtils.hasText(assistantName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "助手名称不能为空");
        }

        String assistantKind = normalizeAssistantKind(source.getAssistantKind());
        if (!StringUtils.hasText(assistantKind)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "助手能力类型不能为空");
        }
        if (!ASSISTANT_KIND_PATTERN.matcher(assistantKind).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "助手能力类型格式不正确，请使用字母开头，仅支持字母、数字、_、-"
            );
        }

        String systemPrompt = AssistantPromptTemplates.resolveInitialPrompt(
                assistantCode,
                assistantKind,
                source.getSystemPrompt()
        );
        if (!StringUtils.hasText(systemPrompt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "系统提示词不能为空");
        }

        target.setAssistantCode(assistantCode);
        target.setAssistantName(assistantName);
        target.setAssistantDesc(trimToNull(source.getAssistantDesc()));
        target.setAssistantKind(assistantKind);
        target.setSystemPrompt(systemPrompt);
        target.setSortOrder(source.getSortOrder() == null ? 0 : source.getSortOrder());
    }

    private void clearDefaultFlagForOthers(Long currentId) {
        LambdaUpdateWrapper<AssistantInfoEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ne(AssistantInfoEntity::getId, currentId)
                .set(AssistantInfoEntity::getIsDefault, 0);
        assistantInfoMapper.update(null, wrapper);
    }

    private void ensureDefaultAssistant() {
        AssistantInfoEntity defaultAssistant = getDefaultAssistant();
        if (defaultAssistant == null || defaultAssistant.getIsDefault() == 1) {
            return;
        }

        AssistantInfoEntity update = new AssistantInfoEntity();
        update.setId(defaultAssistant.getId());
        update.setIsDefault(1);
        update.setEnabled(1);
        update.setUpdateTime(LocalDateTime.now());
        assistantInfoMapper.updateById(update);
        clearDefaultFlagForOthers(defaultAssistant.getId());
    }

    private long countEnabledAssistants() {
        LambdaQueryWrapper<AssistantInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssistantInfoEntity::getEnabled, 1);
        return assistantInfoMapper.selectCount(wrapper);
    }

    private LambdaQueryWrapper<AssistantInfoEntity> baseOrderWrapper() {
        return new LambdaQueryWrapper<AssistantInfoEntity>()
                .orderByDesc(AssistantInfoEntity::getIsDefault)
                .orderByAsc(AssistantInfoEntity::getSortOrder)
                .orderByAsc(AssistantInfoEntity::getId);
    }

    private String normalizeAssistantCode(String assistantCode) {
        String value = trimToNull(assistantCode);
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private String normalizeAssistantKind(String assistantKind) {
        String value = trimToNull(assistantKind);
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    private Integer normalizeFlag(Integer flag, Integer defaultValue) {
        if (flag == null) {
            return defaultValue;
        }
        return flag == 1 ? 1 : 0;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
