package com.studyIn.modules.board;

import com.studyIn.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final ModelMapper modelMapper;

    public Board newWriting(BoardForm boardForm, Account account) {
        Board board = modelMapper.map(boardForm, Board.class);
        board.setSeq(boardRepository.count());
        board.setDateCreated(LocalDateTime.now());
        board.writerRegistering(account);
        return boardRepository.save(board);
    }

    public Board readBoard(Board board) {
        Board readBoard = boardRepository.findById(board.getId()).orElseThrow();
        readBoard.increaseInViews();
        return readBoard;
    }
}
