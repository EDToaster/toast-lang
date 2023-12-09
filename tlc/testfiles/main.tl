
inline or over if else swap end drop end
inline and over if swap end drop end

fn main -> do
    1 1
    while dup 1000000 < do
        dup p
        over + swap
    end

    13 0 and p
    13 14 and p
    12 0 or p

    drop drop

    "hello world" p
    [ 1 2 3 ?? ] ?? p
end