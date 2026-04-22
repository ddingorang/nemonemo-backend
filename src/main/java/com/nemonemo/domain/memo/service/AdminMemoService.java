package com.nemonemo.domain.memo.service;

import com.nemonemo.common.exception.BusinessException;
import com.nemonemo.common.exception.ErrorCode;
import com.nemonemo.domain.memo.dto.MemoRequest;
import com.nemonemo.domain.memo.dto.MemoResponse;
import com.nemonemo.domain.memo.entity.Memo;
import com.nemonemo.domain.memo.repository.MemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemoService {

    private final MemoRepository memoRepository;

    public List<MemoResponse> getMemos() {
        return memoRepository.findAllByOrderByPinnedDescCreatedAtDesc()
                .stream().map(MemoResponse::from).toList();
    }

    @Transactional
    public MemoResponse createMemo(MemoRequest request) {
        Memo memo = Memo.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .pinned(request.isPinned())
                .build();
        return MemoResponse.from(memoRepository.save(memo));
    }

    @Transactional
    public MemoResponse updateMemo(Long id, MemoRequest request) {
        Memo memo = memoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMO_NOT_FOUND));
        memo.update(request.getTitle(), request.getContent(), request.getCategory(), request.isPinned());
        return MemoResponse.from(memo);
    }

    @Transactional
    public void deleteMemo(Long id) {
        if (!memoRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.MEMO_NOT_FOUND);
        }
        memoRepository.deleteById(id);
    }
}
